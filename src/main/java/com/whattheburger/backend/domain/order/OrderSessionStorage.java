package com.whattheburger.backend.domain.order;

import com.whattheburger.backend.domain.enums.OrderStatus;
import com.whattheburger.backend.util.SessionKey;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface OrderSessionStorage {
    void save(String sessionKey, OrderSession orderSession);
    Optional<OrderSession> load(SessionKey sessionKey);
    Optional<String> loadSessionId(SessionKey sessionKey);
    Optional<OrderSession> load(String sessionId);
}
