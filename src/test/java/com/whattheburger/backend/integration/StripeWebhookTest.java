package com.whattheburger.backend.integration;

import com.stripe.model.PaymentIntent;
import com.whattheburger.backend.domain.User;
import com.whattheburger.backend.domain.checkout.CheckoutAttempt;
import com.whattheburger.backend.domain.enums.PaymentStatus;
import com.whattheburger.backend.integration.support.AbstractStripeWebhookIntegrationTest;
import com.whattheburger.backend.security.enums.Role;
import com.whattheburger.backend.service.OrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class StripeWebhookTest extends AbstractStripeWebhookIntegrationTest {

    @SpyBean
    OrderService orderService;

    @AfterEach
    void resetSpies() {
        Mockito.reset(orderService);
    }

    @Test
    void paymentSuccess_whenOrderSessionIsMissing_successfullyCreatesOrder() throws Exception {
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

        try (MockedStatic<PaymentIntent> mocked = stripeWebhookTestSupport.mockPaymentIntentRetrieve(paymentIntentId)) {
            stripeWebhookTestSupport.postWebhook(mockMvc, payload, signature)
                    .andExpect(status().isOk());
        }

        assertOrderCreated(checkoutSessionId, user, scenario.store(), prevOrderCount);
    }

    @Test
    void paymentSuccess_whenOrderSessionDeletedFromRedis_successfullyCreatesOrder() throws Exception {
        CountableScenario scenario = saveCountableScenario(50);
        User user = cartTestSupport.saveUser(Role.USER);
        CheckoutAttempt checkoutAttempt = saveCheckoutAttempt(user, scenario, null);
        saveOrderSessionToRedis(checkoutAttempt, scenario, user);
        orderSessionStorage.remove(checkoutAttempt.getOrderSessionId());

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

        try (MockedStatic<PaymentIntent> mocked = stripeWebhookTestSupport.mockPaymentIntentRetrieve(paymentIntentId)) {
            stripeWebhookTestSupport.postWebhook(mockMvc, payload, signature)
                    .andExpect(status().isOk());
        }

        assertOrderCreated(checkoutSessionId, user, scenario.store(), prevOrderCount);
    }

    @Test
    void paymentSuccess_whenFirstWebhookFailsOnRetry_recoverAndCreatesOrder() throws Exception {
        CountableScenario scenario = saveCountableScenario(50);
        User user = cartTestSupport.saveUser(Role.USER);
        CheckoutAttempt checkoutAttempt = saveCheckoutAttempt(user, scenario, null);
        String checkoutSessionId = checkoutAttempt.getCheckoutSessionId();
        String paymentIntentId = "pi_test_" + UUID.randomUUID().toString().replace("-", "");

        long prevOrderCount = orderRepository.count();
        int prevStock = scenario.storeInventory().getCurrentStock();
        Long storeInventoryId = scenario.storeInventory().getId();

        String payload = stripeWebhookTestSupport.buildCheckoutSessionCompletedPayload(
                checkoutSessionId,
                paymentIntentId,
                checkoutAttempt.getCheckoutAttemptId(),
                checkoutAttempt.getOrderSessionId()
        );
        String signature = stripeWebhookTestSupport.signPayload(payload, webhookSecret);

        doThrow(new RuntimeException("simulated server crash"))
                .doCallRealMethod()
                .when(orderService).completePaidOrder(any(), eq(checkoutSessionId), any());

        try (MockedStatic<PaymentIntent> mocked = stripeWebhookTestSupport.mockPaymentIntentRetrieve(paymentIntentId)) {
            stripeWebhookTestSupport.postWebhook(mockMvc, payload, signature)
                    .andExpect(status().isInternalServerError());
        }

        assertThat(orderRepository.count()).isEqualTo(prevOrderCount);
        assertThat(orderRepository.findByCheckoutSessionId(checkoutSessionId)).isEmpty();
        assertThat(checkoutAttemptRepository.findById(checkoutAttempt.getCheckoutAttemptId()).orElseThrow()
                .getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(storeInventoryRepository.findById(storeInventoryId).orElseThrow().getCurrentStock())
                .isEqualTo(prevStock);

        try (MockedStatic<PaymentIntent> mocked = stripeWebhookTestSupport.mockPaymentIntentRetrieve(paymentIntentId)) {
            stripeWebhookTestSupport.postWebhook(mockMvc, payload, signature)
                    .andExpect(status().isOk());
        }

        assertOrderCreated(checkoutSessionId, user, scenario.store(), prevOrderCount);
        assertThat(checkoutAttemptRepository.findById(checkoutAttempt.getCheckoutAttemptId()).orElseThrow()
                .getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
    }
}
