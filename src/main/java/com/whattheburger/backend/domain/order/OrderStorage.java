package com.whattheburger.backend.domain.order;

import org.springframework.security.core.Authentication;

import java.util.Optional;

public interface OrderStorage {
    Order save(Order order);
    Optional<Order> load(OrderOwnerKey orderOwnerKey);
}
