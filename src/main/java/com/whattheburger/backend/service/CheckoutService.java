package com.whattheburger.backend.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentRetrieveParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.whattheburger.backend.domain.checkout.CheckoutAttempt;
import com.whattheburger.backend.domain.enums.OrderStatus;
import com.whattheburger.backend.domain.enums.PaymentStatus;
import com.whattheburger.backend.domain.order.*;
import com.whattheburger.backend.exception.ApiException;
import com.whattheburger.backend.repository.CheckoutAttemptRepository;
import com.whattheburger.backend.security.UserDetailsImpl;
import com.whattheburger.backend.service.exception.ResourceNotFoundException;
import com.whattheburger.backend.service.exception.order.NonRetryableOrderProcessingException;
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
import java.time.Duration;
import java.util.Map;
import java.util.Random;
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
    private final CheckoutAttemptRepository checkoutAttemptRepository;
    private final OrderTrackingService orderTrackingService;
    private final CartService cartService;
    private final WebhookService webhookService;

    public Session createCheckoutSession(
            CheckoutAttempt checkoutAttempt,
            UUID orderSessionId
    ) {
        Stripe.apiKey = secretKey;

        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .putMetadata("checkoutAttemptId", checkoutAttempt.getCheckoutAttemptId().toString())
                .putMetadata("orderSessionId", orderSessionId.toString())
                .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}");

        SessionCreateParams.PaymentIntentData paymentIntentData = SessionCreateParams.PaymentIntentData
                .builder()
                .putMetadata("checkoutAttemptId", checkoutAttempt.getCheckoutAttemptId().toString())
                .putMetadata("orderSessionId", orderSessionId.toString())
                .build();

        for (OrderSessionProduct orderSessionProduct : checkoutAttempt.getOrderRecord()) {
            BigDecimal totalPrice = orderSessionProduct.getTotalPrice();
            log.info("total price {}", totalPrice);
            BigDecimal priceInCents = totalPrice.multiply(BigDecimal.valueOf(100));
            paramsBuilder
                    .setPaymentIntentData(paymentIntentData)
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
            CheckoutAttempt persistedAttempt = checkoutAttemptRepository.findById(checkoutAttempt.getCheckoutAttemptId())
                    .orElseThrow(() -> new ResourceNotFoundException("checkoutAttempt not found"));
            persistedAttempt.changeCheckoutSessionId(session.getId());
            checkoutAttemptRepository.save(persistedAttempt);
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

    public String getOrderSessionId(String checkoutSessionId) {
        return checkoutAttemptRepository.findByCheckoutSessionId(checkoutSessionId)
                .map(attempt -> attempt.getOrderSessionId().toString())
                .orElseThrow(() -> new ResourceNotFoundException("checkoutSessionId not found"));
    }

    public void handleCheckoutSessionCompleted(
            Event event,
            Session session
    ) {
        String objectId = session.getId();
        String eventType = event.getType();
        String idempotencyKey = eventType + ":" + objectId;

        boolean idempotencyKeyExists = webhookService.processIdempotency(idempotencyKey, session.getId());

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

        OrderSession orderSession = orderService.loadOrderSessionByOrderSessionId(UUID.fromString(orderSessionId));
        orderService.updateOrderSessionPaymentStatus(orderSession, PaymentStatus.PAID);
        markOrderConfirming(orderSession);

        Order order;
        try {
            if (idempotencyKeyExists == false) {
                order = orderService.completePaidOrder(
                        orderSession,
                        session.getId(),
                        paymentMethodObject
                );
            } else {
                order = orderService.loadOrderByCheckoutSessionId(session.getId())
                        .orElseGet(() ->
                                orderService.completePaidOrder(
                                        orderSession,
                                        session.getId(),
                                        paymentMethodObject
                                )
                        );
            }
        } catch(NonRetryableOrderProcessingException e1) {
            try {
                refundPayment(pi.getId());
            } catch (StripeException e2) {
                log.error(e2.getMessage());
                throw new ApiException("Something is wrong with stripe server", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return;
        }

        orderTrackingService.scheduleOrder(orderSession, order);
        cartService.cleanUp(UUID.fromString(cartSessionId));
        orderService.addOrderToOrderSession(order, orderSession);

        log.info("Order ID {}", order.getId());
    }

    private Refund refundPayment(String paymentIntentId) throws StripeException {
        RequestOptions requestOptions =
                RequestOptions.builder()
                        .setIdempotencyKey("refund:" + paymentIntentId)
                        .build();

        RefundCreateParams params =
                RefundCreateParams.builder()
                        .setPaymentIntent(paymentIntentId)
                        .build();

        return Refund.create(params, requestOptions);
    }

    public void handlePaymentIntentSucceeded(
            Event event,
            PaymentIntent paymentIntent
    ) {
        log.info("Payment intent succeeded");
        Map<String, String> metadata = paymentIntent.getMetadata();
        String orderSessionId = metadata.get("orderSessionId");
        log.info("Order session id: {}", orderSessionId);
        OrderSession orderSession = orderService.loadOrderSessionByOrderSessionId(UUID.fromString(orderSessionId));
        orderService.updateOrderSessionPaymentStatus(orderSession, PaymentStatus.PENDING);
        log.info("Order payment status: {}", orderSession.getPaymentStatus());
    }

    private void markOrderConfirming(
            OrderSession orderSession
    ) {
        if (orderSession.getOrderStatus() == OrderStatus.CONFIRMING) {
            return;
        }

        int randomDuration = new Random().nextInt(20, 30) * 1000;

        orderSession.updateOrderStatus(
                OrderStatus.CONFIRMING,
                System.currentTimeMillis(),
                randomDuration
        );
    }

    private String getCartSessionKey(UUID guestId, Long storeId, Authentication authentication) {
        boolean isUser = authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);
        log.info("isUser {}", isUser);
        if (isUser) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails userDetails) {
                return "cart:store:" + storeId + ":" + userDetails.getUsername();
            }
            log.info("Principal {}", principal);
        }
        return "cart:store:" + storeId + ":" + guestId;
    }
}
