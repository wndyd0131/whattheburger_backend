package com.whattheburger.backend.service;

import org.springframework.stereotype.Service;

public interface WebhookService {
    public boolean processIdempotency(String key, String checkoutSessionId);
}
