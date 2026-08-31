package com.whattheburger.backend.domain.checkout;

import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.whattheburger.backend.domain.order.OrderSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class RedisWebhookIdempotencyStorage implements IdempotencyStorage {
    private final StringRedisTemplate stringRedisTemplate;
    private final Duration IDEMPOTENCY_KEY_EXPIRATION_MINUTES = Duration.ofMinutes(5);

    @Override
    public boolean tryAcquire(String key) {
        return stringRedisTemplate.opsForValue().setIfAbsent(key, "", IDEMPOTENCY_KEY_EXPIRATION_MINUTES);
    }

    @Override
    public void save(String key) {
        stringRedisTemplate.opsForValue().set(key, "", IDEMPOTENCY_KEY_EXPIRATION_MINUTES);
    }
}
