package com.whattheburger.backend.repository.checkout;

import java.util.Optional;
import java.util.UUID;

public interface CheckoutSessionStorage {
    public void save(String checkoutSessionId, UUID orderSessionId);
    public Optional<String> getOrderSessionId(String checkoutSessionId);
}
