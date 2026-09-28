package com.whattheburger.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.whattheburger.backend.controller.dto.order.DeliveryOrderFormRequestDto;
import com.whattheburger.backend.controller.dto.order.OrderFormRequestDto;
import com.whattheburger.backend.domain.checkout.CheckoutAttempt;
import com.whattheburger.backend.integration.support.BaseIntegrationTest;
import com.whattheburger.backend.repository.CheckoutAttemptRepository;
import com.whattheburger.backend.service.CheckoutService;
import com.whattheburger.backend.service.S3Service;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class CheckoutFlowIntegrationTest extends BaseIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    CheckoutAttemptRepository checkoutAttemptRepository;

    @MockBean
    S3Service s3Service;

    @SpyBean
    CheckoutService checkoutService;

    @AfterEach
    void resetSpies() {
        Mockito.reset(checkoutService);
    }

    @Test
    void createCheckout_whenOrderSessionMissing_returns404AndNeverCreatesStripeSession() throws Exception {
        UUID missingOrderSessionId = UUID.randomUUID();
        UUID guestId = UUID.randomUUID();
        long prevCheckoutAttemptCount = checkoutAttemptRepository.count();

        OrderFormRequestDto formRequest = new DeliveryOrderFormRequestDto(
                null,
                "Test",
                "User",
                "123 Main St",
                "Apt 1",
                "78701",
                "Austin, TX",
                "checkout-test@example.com",
                "5121234567"
        );

        mockMvc.perform(post("/api/v1/checkout/{orderSessionId}", missingOrderSessionId)
                        .cookie(new Cookie("guestId", guestId.toString()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(formRequest)))
                .andExpect(status().isNotFound());

        assertThat(checkoutAttemptRepository.count()).isEqualTo(prevCheckoutAttemptCount);
        verify(checkoutService, never()).createCheckoutSession(any(CheckoutAttempt.class), any(UUID.class));
    }
}
