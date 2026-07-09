package com.whattheburger.backend.service.exception.cart;

import com.whattheburger.backend.service.exception.BadRequestException;

public class CartStoreProductNotFoundException extends BadRequestException {
    public CartStoreProductNotFoundException(Long storeProductId) {
        super("Store product with ID[" + storeProductId + "] not found in cart");
    }
}
