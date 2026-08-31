package com.whattheburger.backend.service.exception.cart;

import com.whattheburger.backend.service.exception.BadRequestException;

public class StoreProductNotInStoreException extends BadRequestException {
    public StoreProductNotInStoreException(Long storeProductId, Long storeId) {
        super(String.format(
                "Store product [%d] is not available at store [%d]",
                storeProductId,
                storeId));
    }
}
