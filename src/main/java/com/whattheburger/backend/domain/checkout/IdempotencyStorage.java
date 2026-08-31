package com.whattheburger.backend.domain.checkout;

public interface IdempotencyStorage {
    boolean tryAcquire(String key);
    void save(String key);
}
