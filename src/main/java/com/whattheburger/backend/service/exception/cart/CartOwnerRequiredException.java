package com.whattheburger.backend.service.exception.cart;

import com.whattheburger.backend.service.exception.BadRequestException;

public class CartOwnerRequiredException extends BadRequestException {
    public CartOwnerRequiredException() {
        super("Guest ID or authenticated user is required");
    }
}
