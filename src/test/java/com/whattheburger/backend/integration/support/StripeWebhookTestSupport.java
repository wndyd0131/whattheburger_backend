package com.whattheburger.backend.integration.support;

import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentRetrieveParams;
import org.mockito.MockedStatic;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Component
public class StripeWebhookTestSupport {

    public String buildCheckoutSessionCompletedPayload(
            String checkoutSessionId,
            String paymentIntentId,
            UUID orderSessionId,
            UUID cartSessionId
    ) {
        String eventId = "evt_test_" + UUID.randomUUID().toString().replace("-", "");
        return """
                {
                  "id": "%s",
                  "object": "event",
                  "api_version": "2024-06-20",
                  "created": 1710000000,
                  "type": "checkout.session.completed",
                  "data": {
                    "object": {
                      "id": "%s",
                      "object": "checkout.session",
                      "payment_intent": "%s",
                      "metadata": {
                        "orderSessionId": "%s",
                        "cartSessionId": "%s"
                      }
                    }
                  }
                }
                """.formatted(
                eventId,
                checkoutSessionId,
                paymentIntentId,
                orderSessionId,
                cartSessionId
        );
    }

    public String signPayload(String payload, String webhookSecret) {
        long timestamp = System.currentTimeMillis() / 1000;
        return Webhook.generateTestHeaderString(payload, webhookSecret, timestamp);
    }

    public MockedStatic<PaymentIntent> mockPaymentIntentRetrieve(String paymentIntentId) {
        MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class);
        PaymentIntent paymentIntent = mock(PaymentIntent.class);
        PaymentMethod paymentMethod = mock(PaymentMethod.class);

        when(paymentIntent.getPaymentMethodObject()).thenReturn(paymentMethod);

        mockedStatic.when(() -> PaymentIntent.retrieve(
                eq(paymentIntentId),
                any(PaymentIntentRetrieveParams.class),
                isNull()
        )).thenReturn(paymentIntent);

        return mockedStatic;
    }

    public ResultActions postWebhook(MockMvc mockMvc, String payload, String signature) throws Exception {
        return mockMvc.perform(post("/api/v1/checkout/webhook")
                .header("Stripe-Signature", signature)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));
    }
}
