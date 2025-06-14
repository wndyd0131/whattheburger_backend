package com.whattheburger.backend.service.exception.cart;

import com.whattheburger.backend.service.exception.BadRequestException;

public class InvalidCartIndexException extends BadRequestException {
    public InvalidCartIndexException(int idx) {
        super("No item at index " + idx);
    }
}
