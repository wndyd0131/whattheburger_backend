package com.whattheburger.backend.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.whattheburger.backend.domain.enums.OrderStatus;
import com.whattheburger.backend.domain.enums.PaymentMethod;
import com.whattheburger.backend.domain.enums.PaymentStatus;
import com.whattheburger.backend.domain.order.*;
import com.whattheburger.backend.security.UserDetailsImpl;
import com.whattheburger.backend.service.exception.order.OrderSessionNotFoundException;
import com.whattheburger.backend.util.SessionKey;
import com.whattheburger.backend.util.UserType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
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

    public Session createCheckoutSession(
            UUID guestId,
            Authentication authentication
    ) {
        // find order
        SessionKey sessionKey = getSessionKey(guestId, authentication);
        String sessionId = orderSessionStorage.loadSessionId(sessionKey)
                .orElseThrow(() -> new OrderSessionNotFoundException(sessionKey.key()));
        OrderSession orderSession = orderService.loadOrderSession(guestId, authentication);// get order by user/guest? or by orderId?

        Stripe.apiKey = secretKey;
        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .putMetadata("orderSessionId", sessionId)
                .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}");


        for (OrderSessionProduct orderSessionProduct : orderSession.getOrderSessionProducts()) {
            BigDecimal totalPrice = orderSessionProduct.getTotalPrice();
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
        Map<String, String> metadata = session.getMetadata();
        log.info("Checkout Session Id {}", session.getId());
        String orderSessionId = metadata.get("orderSessionId");
        if (orderSessionId == null)
            throw new IllegalStateException("key orderSessionId does not exist in stripe metadata");

        Order order = orderSessionStorage.load(orderSessionId)
                .map(orderSession -> orderService.transferFromOrderSession(orderSession))
                .orElseThrow(() -> new OrderSessionNotFoundException(orderSessionId));
        order.changeOrderStatus(OrderStatus.CONFIRMED);
        order.changePaymentStatus(PaymentStatus.PAID);
        order.changePaymentMethod(PaymentMethod.CASH);
        order.changeCheckoutSessionId(session.getId());
        orderService.saveOrder(order);
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
}
