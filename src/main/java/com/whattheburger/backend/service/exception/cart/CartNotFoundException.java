package com.whattheburger.backend.service.exception.cart;

import com.whattheburger.backend.service.exception.ResourceNotFoundException;

import java.util.UUID;

public class CartNotFoundException extends ResourceNotFoundException {
    public CartNotFoundException(UUID sessionId) {
        super("Cart with UUID[" + sessionId + "] not found");
    }
    public CartNotFoundException(String sessionKey) {
        super("Cart with Key[" + sessionKey + "] not found");
    }
}
