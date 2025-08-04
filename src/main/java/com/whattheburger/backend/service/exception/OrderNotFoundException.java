package com.whattheburger.backend.service.exception;

import java.util.UUID;

public class OrderNotFoundException extends ResourceNotFoundException {
    public OrderNotFoundException(Long userId) {
        super("Available order with user ID[" + userId + "] not found");
    }

    public OrderNotFoundException(UUID guestId) {
        super("Available order with guest ID[" + guestId + "] not found");
    }
}
