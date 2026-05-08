package com.whattheburger.backend.service.exception.cart;

import com.whattheburger.backend.service.exception.BadRequestException;

public class StoreProductNotActiveException extends BadRequestException {
    public StoreProductNotActiveException(Long storeProductId) {
        super(String.format("Store product [%d] is not active", storeProductId));
    }
}
