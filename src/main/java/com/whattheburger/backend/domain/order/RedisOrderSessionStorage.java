package com.whattheburger.backend.domain.order;

import com.whattheburger.backend.domain.cart.CartList;
import com.whattheburger.backend.util.SessionKey;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RedisOrderSessionStorage implements OrderSessionStorage {
    private final RedisTemplate<String, OrderSession> rt;
    private final StringRedisTemplate stringRedisTemplate;
    private final long ORDER_SESSION_EXPIRATION_MINUTES = 5;

    @Override
    public void save(String sessionKey, OrderSession orderSession) {
        rt.opsForValue().set(sessionKey, orderSession);
    }

    @Override
    public void save(OrderSession orderSession) {
        String sessionKey = getSessionKey(orderSession.getSessionId());
        rt.opsForValue().set(sessionKey, orderSession, Duration.ofMinutes(ORDER_SESSION_EXPIRATION_MINUTES));
    }

    @Override
    public Optional<OrderSession> load(SessionKey sessionKey) throws OrderSessionKeyNotFoundException {
        return Optional.ofNullable(stringRedisTemplate.opsForValue().get("order:" + sessionKey.key()))
                .flatMap(sessionId -> Optional.ofNullable(rt.opsForValue().get(sessionId)));
    }

    @Override
    public Optional<OrderSession> load(String sessionKey) {
        return Optional.ofNullable(rt.opsForValue().get(sessionKey));
    }

    @Override
    public Optional<OrderSession> load(UUID orderSessionId) {
        if (orderSessionId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(rt.opsForValue().get(getSessionKey(orderSessionId)));
    }

    @Override
    public void remove(UUID orderSessionId) {
        String sessionKey = getSessionKey(orderSessionId);
        rt.delete(sessionKey);
    }

    private String getSessionKey(UUID orderSessionId) {
        if (orderSessionId == null)
            return null;
        return "order:" + orderSessionId;
    }
}
