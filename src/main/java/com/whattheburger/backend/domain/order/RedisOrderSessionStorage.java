package com.whattheburger.backend.domain.order;

import com.whattheburger.backend.domain.cart.CartList;
import com.whattheburger.backend.util.SessionKey;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RedisOrderSessionStorage implements OrderSessionStorage {
    private final RedisTemplate<String, OrderSession> rt;
    private final StringRedisTemplate stringRedisTemplate;
    @Override
    public void save(String sessionKey, OrderSession orderSession) {
        UUID sessionId = orderSession.getSessionId();
        stringRedisTemplate.opsForValue().set("order:" + sessionKey, sessionId.toString());
        rt.opsForValue().set(sessionId.toString(), orderSession);
    }

    @Override
    public Optional<OrderSession> load(SessionKey sessionKey) throws OrderSessionKeyNotFoundException {
        return Optional.ofNullable(stringRedisTemplate.opsForValue().get("order:" + sessionKey.key()))
                .flatMap(orderSessionKey -> Optional.ofNullable(rt.opsForValue().get(orderSessionKey)));
    }

    @Override
    public Optional<String> loadSessionId(SessionKey sessionKey) {
        return Optional.ofNullable(stringRedisTemplate.opsForValue().get("order:" + sessionKey.key()));
    }

    @Override
    public Optional<OrderSession> load(UUID sessionId) {
        return Optional.ofNullable(rt.opsForValue().get(sessionId.toString()));
    }

    @Override
    public void remove(UUID sessionId) {
        rt.delete(sessionId.toString());
    }
}
