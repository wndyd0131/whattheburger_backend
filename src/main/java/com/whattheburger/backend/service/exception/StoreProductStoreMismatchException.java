package com.whattheburger.backend.service.exception;

public class StoreProductStoreMismatchException extends BadRequestException {
    public StoreProductStoreMismatchException(Long storeId, Long storeProductId) {
        super("StoreProduct with ID[" + storeProductId + "] does not belong to store with ID[" + storeId + "]");
    }
}
