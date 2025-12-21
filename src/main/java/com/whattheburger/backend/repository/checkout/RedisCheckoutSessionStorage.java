package com.whattheburger.backend.repository.checkout;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RedisCheckoutSessionStorage implements CheckoutSessionStorage {
    private final StringRedisTemplate stringRedisTemplate;
    @Override
    public void save(String checkoutSessionId, UUID orderSessionId) {
        String sessionKey = getSessionKey(checkoutSessionId);
        stringRedisTemplate.opsForValue().set(sessionKey, orderSessionId.toString());
    }

    @Override
    public Optional<String> getOrderSessionId(String checkoutSessionId) {
        String sessionKey = getSessionKey(checkoutSessionId);
        return Optional.ofNullable(stringRedisTemplate.opsForValue().get(sessionKey));
    }

    private String getSessionKey(String checkoutSessionId) {
        return "checkout:" + checkoutSessionId;
    }
}
