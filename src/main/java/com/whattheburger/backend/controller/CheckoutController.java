package com.whattheburger.backend.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.whattheburger.backend.controller.dto.CheckoutRequestDto;
import com.whattheburger.backend.controller.dto.CheckoutResponseDto;
import com.whattheburger.backend.controller.dto.order.OrderFormRequestDto;
import com.whattheburger.backend.domain.order.OrderSession;
import com.whattheburger.backend.service.CheckoutService;
import com.whattheburger.backend.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import retrofit2.http.Path;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
public class CheckoutController {

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    private final CheckoutService checkoutService;
    private final OrderService orderService;

    @PostMapping("/api/v1/checkout")
    public ResponseEntity<CheckoutResponseDto> createCheckoutSession(
            @RequestBody OrderFormRequestDto formRequestDto,
            @CookieValue(name = "guestId") UUID guestId,
            @CookieValue(name = "orderSessionId")  UUID orderSessionId,
            Authentication authentication
    ) {
        OrderSession orderSession = orderService.updateOrderSession(formRequestDto, orderSessionId, authentication, guestId);
        Session checkoutSession = checkoutService.createCheckoutSession(orderSession, guestId, authentication);
        String redirectUrl = checkoutSession.getUrl();
        log.info("URL {}", redirectUrl);
        return new ResponseEntity<>(
                new CheckoutResponseDto(redirectUrl),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/api/v1/checkout/webhook")
    public Object handleWebHook(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) throws IOException {
        byte[] body = request.getInputStream().readAllBytes();
        String payload = new String(body, StandardCharsets.UTF_8);
        log.info("payload {}", payload);

        Event event = null;
        if (endpointSecret != null && sigHeader != null) {
            try {
                event = Webhook.constructEvent(
                        payload,
                        sigHeader,
                        endpointSecret
                );
            } catch (SignatureVerificationException e) {
                log.info("endpointSecret {}", endpointSecret);
                log.info("sigHeader {}", sigHeader);
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return "";
            }
        }

        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = null;
        if (dataObjectDeserializer.getObject().isPresent()) {
            stripeObject = dataObjectDeserializer.getObject().get();
        } else {
            return "";
        }

        switch (event.getType()) {
            case "checkout.session.completed":
                Session session = (Session) stripeObject;
                log.info("Checkout Session {}", session);
                checkoutService.handleCheckoutSessionCompleted(
                        session
                );
                break;
            case "payment_intent.succeeded":
                PaymentIntent paymentIntent = (PaymentIntent) stripeObject;
                log.info("Payment Intent {}", paymentIntent);
                checkoutService.handlePaymentIntentSucceeded(
                        paymentIntent
                );
                break;
            case "payment_method.attached":
                PaymentMethod paymentMethod = (PaymentMethod) stripeObject;
                break;
            default:
                System.out.println("Unhandled event type: " + event.getType());
        }
        response.setStatus(HttpServletResponse.SC_OK);
        return "";
    }
}
