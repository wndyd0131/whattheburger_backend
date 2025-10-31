package com.whattheburger.backend.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.PaymentIntentRetrieveParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.whattheburger.backend.config.websocket.OrderTrackingWebSocketHandler;
import com.whattheburger.backend.domain.cart.CartList;
import com.whattheburger.backend.domain.enums.OrderStatus;
import com.whattheburger.backend.domain.enums.PaymentMethod;
import com.whattheburger.backend.domain.enums.PaymentStatus;
import com.whattheburger.backend.domain.order.*;
import com.whattheburger.backend.exception.ApiException;
import com.whattheburger.backend.security.UserDetailsImpl;
import com.whattheburger.backend.service.exception.order.OrderSessionNotFoundException;
import com.whattheburger.backend.util.SessionKey;
import com.whattheburger.backend.util.UserType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutService {
    @Value("${stripe.secret}")
    private String secretKey;
    @Value("${stripe.successUrl}")
    private String successUrl;

    private final OrderService orderService;
    private final OrderSessionStorage orderSessionStorage;
    private final OrderFactory orderFactory;
    private final OrderTrackingService orderTrackingService;
    private final CartService cartService;

    public Session createCheckoutSession(
            OrderSession orderSession,
            UUID guestId,
            Authentication authentication
    ) {
        // find order

        UUID orderSessionId = orderSession.getSessionId();
        Long storeId = orderSession.getStoreId();
        String cartSessionKey = getCartSessionKey(guestId, storeId, authentication);
        UUID cartSessionId = cartService.getSessionId(cartSessionKey);

        Stripe.apiKey = secretKey;
        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .putMetadata("orderSessionId", orderSessionId.toString())
                .putMetadata("cartSessionId", cartSessionId.toString())
                .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}");

        for (OrderSessionProduct orderSessionProduct : orderSession.getOrderSessionProducts()) {
            BigDecimal totalPrice = orderSessionProduct.getTotalPrice();
            log.info("total price {}", totalPrice);
            BigDecimal priceInCents = totalPrice.multiply(BigDecimal.valueOf(100));
            paramsBuilder
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setPriceData(SessionCreateParams.LineItem.PriceData
                                            .builder()
                                            .setCurrency("usd")
                                            .setProductData(
                                                    SessionCreateParams.LineItem.PriceData.ProductData
                                                            .builder()
                                                            .setName(orderSessionProduct.getName())
                                                            .build()
                                            )
                                            .setUnitAmount(priceInCents.longValueExact())
                                            .build())
                                    .setQuantity(Integer.toUnsignedLong(orderSessionProduct.getQuantity()))
                                    .build()
                    );
        }

        SessionCreateParams params = paramsBuilder.build();

        try {
            Session session = Session.create(params);
            return session;
        } catch (StripeException e) {
            e.printStackTrace();
            throw new IllegalStateException("Something's wrong with the payment server");
        }
    }

    private SessionKey getSessionKey(UUID guestId, Authentication authentication) {
        boolean isUser = authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);
        log.info("isUser {}", isUser);
        if (isUser) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetailsImpl userDetailsImpl) {
                return new SessionKey(UserType.USER, "user:" + userDetailsImpl.getUsername());
            }
            log.info("Principal {}", principal);
        }
        return new SessionKey(UserType.GUEST, "guest:" + guestId.toString());
    }


    public void handleCheckoutSessionCompleted(
            Session session
    ) {

        String piId = session.getPaymentIntent();
        log.info("piId {}", piId);
        if (piId == null) {
            log.warn("No payment_intent on session {}", session.getId());
            return;
        }

        PaymentIntentRetrieveParams piParams = PaymentIntentRetrieveParams.builder()
                .addExpand("payment_method")
                .addExpand("latest_charge")
                .build();

        PaymentIntent pi;
        try {
            pi = PaymentIntent.retrieve(piId, piParams, null);
        } catch (StripeException e) {
            log.error(e.getMessage());
            throw new ApiException("Something is wrong with stripe server", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        com.stripe.model.PaymentMethod paymentMethodObject = pi.getPaymentMethodObject();

//        String brand = session.getPaymentIntentObject().getPaymentMethodObject().getCard().getBrand();
//        String last4 = session.getPaymentIntentObject().getPaymentMethodObject().getCard().getLast4();
//        log.info("brand info {}", brand);
//        log.info("las4 info {}", last4);
        Map<String, String> metadata = session.getMetadata();
        log.info("Checkout Session Id {}", session.getId());
        String orderSessionId = metadata.get("orderSessionId");
        String cartSessionId = metadata.get("cartSessionId");
        if (orderSessionId == null)
            throw new IllegalStateException("key orderSessionId does not exist in stripe metadata");

        Order order = orderService.transferFromOrderSession(UUID.fromString(orderSessionId));

        order.changeOrderStatus(OrderStatus.CONFIRMED);
        order.changePaymentStatus(PaymentStatus.PAID);

        if (paymentMethodObject != null && paymentMethodObject.getCard() != null) {
            order.changePaymentMethod(PaymentMethod.CREDIT_CARD);
            String brand = paymentMethodObject.getCard().getBrand();
            String last4 = paymentMethodObject.getCard().getLast4();
            Long expMonth = paymentMethodObject.getCard().getExpMonth();
            Long expYear = paymentMethodObject.getCard().getExpYear();
            order.changeCardInfo(brand, last4, expMonth, expYear);
            log.info("Card {} ****{} exp {}/{}", brand, last4, expMonth, expYear);
        }

        cartService.cleanUp(UUID.fromString(cartSessionId));
        orderService.cleanUp(UUID.fromString(orderSessionId));

        order.changeCheckoutSessionId(session.getId());
        Order savedOrder = orderService.saveOrder(order);

        orderTrackingService.sendReadyFlag(order.getOrderNumber().toString());
        log.info("Order ID {}", savedOrder.getId());
    }

//    public void handlePaymentIntentSucceeded(
//            PaymentIntent paymentIntent
//    ) {
//        Map<String, String> metadata = paymentIntent.getMetadata();
//        String sessionId = metadata.get("orderSessionId");
//        if (sessionId == null)
//            throw new IllegalStateException("key orderSessionId does not exist in stripe metadata");
//        Order order = orderSessionStorage.load(sessionId)
//                .map(orderSession -> orderFactory.createFromOrderSession(orderSession))
//                .orElseThrow(() -> new OrderSessionNotFoundException(sessionId));
//        order.changeOrderStatus(OrderStatus.CONFIRMED);
//        order.changePaymentStatus(PaymentStatus.PAID);
//        order.changePaymentMethod(PaymentMethod.CASH);
//        orderService.saveOrder(order);
//    }

    private String getCartSessionKey(UUID guestId, Long storeId, Authentication authentication) {
        boolean isUser = authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);
        log.info("isUser {}", isUser);
        if (isUser) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails userDetails) {
                return "cart:store" + storeId + ":" + userDetails.getUsername();
            }
            log.info("Principal {}", principal);
        }
        return "cart:store" + storeId + ":" + guestId;
    }
}
