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
import com.whattheburger.backend.domain.enums.PaymentStatus;
import com.whattheburger.backend.domain.order.Order;
import com.whattheburger.backend.domain.order.OrderSession;
import com.whattheburger.backend.domain.order.OrderSessionProduct;
import com.whattheburger.backend.exception.ApiException;
import com.whattheburger.backend.repository.CheckoutAttemptRepository;
import com.whattheburger.backend.service.exception.ResourceNotFoundException;
import com.whattheburger.backend.service.exception.order.NonRetryableOrderProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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
    private final CheckoutAttemptRepository checkoutAttemptRepository;
    private final CartService cartService;

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

    public String getOrderSessionId(String checkoutSessionId) {
        return checkoutAttemptRepository.findByCheckoutSessionId(checkoutSessionId)
                .map(attempt -> attempt.getOrderSessionId().toString())
                .orElseThrow(() -> new ResourceNotFoundException("checkoutSessionId not found"));
    }

    public void handleCheckoutSessionCompleted(
            Event event,
            Session session
    ) {
        if (orderService.loadOrderByCheckoutSessionId(session.getId()).isPresent()) {
            log.info("Order already exists for checkout session {}", session.getId());
            return;
        }

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

        Map<String, String> metadata = session.getMetadata();
        log.info("Checkout Session Id {}", session.getId());
        String checkoutAttemptId = metadata.get("checkoutAttemptId");
        CheckoutAttempt checkoutAttempt = resolveCheckoutAttempt(checkoutAttemptId, session.getId());

        Order order;
        try {
            order = orderService.completePaidOrder(
                    checkoutAttempt,
                    session.getId(),
                    paymentMethodObject
            );
        } catch (NonRetryableOrderProcessingException e1) {
            orderService.markCheckoutAttemptRefunded(checkoutAttempt.getCheckoutAttemptId());
            try {
                refundPayment(pi.getId());
            } catch (StripeException e2) {
                log.error(e2.getMessage());
                throw new ApiException("Something is wrong with stripe server", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return;
        }

        if (checkoutAttempt.getCartSessionId() != null) {
            cartService.cleanUp(checkoutAttempt.getCartSessionId());
        }

//        orderTrackingService.scheduleOrder(orderSession, order);
//        orderService.addOrderToOrderSession(order, orderSession);

        log.info("Order ID {}", order.getId());
    }

    private CheckoutAttempt resolveCheckoutAttempt(String checkoutAttemptId, String checkoutSessionId) {
        if (checkoutAttemptId != null) {
            return checkoutAttemptRepository.findById(UUID.fromString(checkoutAttemptId))
                    .orElseThrow(() -> new ResourceNotFoundException("checkoutAttempt not found"));
        }
        return checkoutAttemptRepository.findByCheckoutSessionId(checkoutSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("checkoutAttempt not found"));
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
}
