package com.whattheburger.backend.service.exception;

public class StoreInventoryNotFoundException extends ResourceNotFoundException {
    public StoreInventoryNotFoundException(Long storeId, Long ingredientId) {
        super("StoreInventory for store [" + storeId + "] and ingredient [" + ingredientId + "] not found");
    }
}
