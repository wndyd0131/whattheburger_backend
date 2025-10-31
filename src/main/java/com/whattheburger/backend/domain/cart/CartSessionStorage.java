package com.whattheburger.backend.domain.cart;

import com.whattheburger.backend.domain.order.OrderSession;

import java.util.Optional;
import java.util.UUID;

public interface CartSessionStorage {
    void save(String sessionKey, CartList cartList);
    Optional<CartList> load(String sessionKey);
    void remove(UUID sessionId);
}
