package com.whattheburger.backend.domain.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RedisCartSessionStorage implements CartSessionStorage{
    private final RedisTemplate<String, CartList> rt;
    private final StringRedisTemplate stringRedisTemplate;
    @Override
    public void save(String sessionKey, CartList cartList) {
        UUID sessionId = cartList.getSessionId();
        stringRedisTemplate.opsForValue().set(sessionKey, sessionId.toString());
        rt.opsForValue().set(sessionId.toString(), cartList);
    }

    @Override
    public Optional<CartList> load(String sessionKey) {
        return Optional.ofNullable(stringRedisTemplate.opsForValue().get(sessionKey))
                .flatMap(cartSessionKey -> Optional.ofNullable(rt.opsForValue().get(cartSessionKey)));
    }

    @Override
    public void remove(UUID sessionId) {
        rt.delete(sessionId.toString());
    }
}
