package com.whattheburger.backend.service.exception;

public class StoreProductNotFoundException extends ResourceNotFoundException {
    public StoreProductNotFoundException(Long storeProductId) {
        super("StoreProduct with ID[" + storeProductId + "] not found");
    }
}
