package com.whattheburger.backend.service;

import com.whattheburger.backend.domain.checkout.IdempotencyStorage;
import com.whattheburger.backend.domain.order.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StripeWebhookService implements WebhookService {
    private final IdempotencyStorage idempotencyStorage;

    @Override
    public boolean processIdempotency(String key, String checkoutSessionId) {
        boolean acquired = idempotencyStorage.tryAcquire(key);
        if (!acquired) { // idempotency key already exists
            return true;
        }
        return false;
    }
}
