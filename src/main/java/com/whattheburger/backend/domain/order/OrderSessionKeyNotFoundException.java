package com.whattheburger.backend.domain.order;

public class OrderSessionKeyNotFoundException extends RuntimeException {
    public OrderSessionKeyNotFoundException() {
        super("Order key not found");
    }
}
