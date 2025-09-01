package com.whattheburger.backend.service.exception;

import java.util.UUID;

public class OrderNotFoundException extends ResourceNotFoundException {
    public OrderNotFoundException() {
        super("Order not found");
    }
    public OrderNotFoundException(Long userId) {
        super("Available order with user ID[" + userId + "] not found");
    }
    public OrderNotFoundException(String msg) {
        super(msg);
    }

    public static OrderNotFoundException forGuest(UUID guestId) {
        return new OrderNotFoundException("Available order with guest ID[" + guestId + "] not found");
    }
    public static OrderNotFoundException forOrder(UUID orderNumber) {
        return new OrderNotFoundException("Available order with order ID[" + orderNumber + "] not found");
    }
}
