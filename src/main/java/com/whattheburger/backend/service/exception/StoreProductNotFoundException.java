package com.whattheburger.backend.service.exception;

public class StoreProductNotFoundException extends ResourceNotFoundException {
    public StoreProductNotFoundException(Long storeId, Long productId) {
        super("StoreProduct with Store ID[" + storeId + "] and Product ID[" + productId + "] not found");
    }
}
