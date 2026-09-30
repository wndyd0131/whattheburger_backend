package com.whattheburger.backend.integration;

import com.stripe.model.PaymentIntent;
import com.whattheburger.backend.domain.User;
import com.whattheburger.backend.domain.checkout.CheckoutAttempt;
import com.whattheburger.backend.integration.support.AbstractStripeWebhookIntegrationTest;
import com.whattheburger.backend.security.enums.Role;
import org.junit.jupiter.api.AfterEach;
import org.mockito.MockedStatic;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Redis container stop/start tests isolated from {@link StripeWebhookTest}
 * so shared Spring Redis connections are not affected during the main webhook suite.
 */
public class StripeWebhookWithRedisDownTest extends AbstractStripeWebhookIntegrationTest {

    @AfterEach
    void restoreRedisAfterTest() {
        if (!redisContainer().isRunning()) {
            redisContainer().start();
        }
    }

    @Test
    void paymentSuccess_whenRedisIsUnavailable_successfullyCreatesOrder() throws Exception {
        CountableScenario scenario = saveCountableScenario(50);
        User user = cartTestSupport.saveUser(Role.USER);
        CheckoutAttempt checkoutAttempt = saveCheckoutAttempt(user, scenario, null);
        String checkoutSessionId = checkoutAttempt.getCheckoutSessionId();
        String paymentIntentId = "pi_test_" + UUID.randomUUID().toString().replace("-", "");

        long prevOrderCount = orderRepository.count();
        String payload = stripeWebhookTestSupport.buildCheckoutSessionCompletedPayload(
                checkoutSessionId,
                paymentIntentId,
                checkoutAttempt.getCheckoutAttemptId(),
                checkoutAttempt.getOrderSessionId()
        );
        String signature = stripeWebhookTestSupport.signPayload(payload, webhookSecret);

        redisContainer().stop();

        try (MockedStatic<PaymentIntent> mocked = stripeWebhookTestSupport.mockPaymentIntentRetrieve(paymentIntentId)) {
            stripeWebhookTestSupport.postWebhook(mockMvc, payload, signature)
                    .andExpect(status().isOk());
        } finally {
            if (!redisContainer().isRunning()) {
                redisContainer().start();
            }
        }

        assertOrderCreated(checkoutSessionId, user, scenario.store(), prevOrderCount);
    }

//    @Test
//    void paymentSuccess_whenRedisDownDuringCartCleanup_stillCreatesOrder() throws Exception {
//        CountableScenario scenario = saveCountableScenario(50);
//        User user = cartTestSupport.saveUser(Role.USER);
//        UUID cartSessionId = UUID.randomUUID();
//        CheckoutAttempt checkoutAttempt = saveCheckoutAttempt(user, scenario, cartSessionId);
//        String checkoutSessionId = checkoutAttempt.getCheckoutSessionId();
//        String paymentIntentId = "pi_test_" + UUID.randomUUID().toString().replace("-", "");
//
//        long prevOrderCount = orderRepository.count();
//        String payload = stripeWebhookTestSupport.buildCheckoutSessionCompletedPayload(
//                checkoutSessionId,
//                paymentIntentId,
//                checkoutAttempt.getCheckoutAttemptId(),
//                checkoutAttempt.getOrderSessionId()
//        );
//        String signature = stripeWebhookTestSupport.signPayload(payload, webhookSecret);
//
//        redisContainer().stop();
//
//        try (MockedStatic<PaymentIntent> mocked = stripeWebhookTestSupport.mockPaymentIntentRetrieve(paymentIntentId)) {
//            stripeWebhookTestSupport.postWebhook(mockMvc, payload, signature)
//                    .andExpect(status().isInternalServerError());
//        } finally {
//            if (!redisContainer().isRunning()) {
//                redisContainer().start();
//            }
//        }
//
//        assertOrderCreated(checkoutSessionId, user, scenario.store(), prevOrderCount);
//    }
}
