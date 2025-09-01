package com.whattheburger.backend.service.exception.order;

import com.whattheburger.backend.service.exception.ResourceNotFoundException;
import com.whattheburger.backend.util.SessionKey;

import java.util.UUID;

public class OrderSessionNotFoundException extends ResourceNotFoundException {
    public OrderSessionNotFoundException(String sessionKey) {
        super("Order session with UUID[" + sessionKey + "] not found");
    }
}
