package com.whattheburger.backend.service.exception;

public class StoreNotFoundException extends ResourceNotFoundException {
    public StoreNotFoundException(Long storeId) {
        super("Store with ID[" + storeId + "] not found");
    }
}
