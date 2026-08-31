package com.whattheburger.backend.service.exception.cart;

import com.whattheburger.backend.service.exception.BadRequestException;

public class CartItemLimitExceededException extends BadRequestException {
    public CartItemLimitExceededException(int maxItems) {
        super("Cart cannot contain more than " + maxItems + " items");
    }
}
