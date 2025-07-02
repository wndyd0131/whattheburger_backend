package com.whattheburger.backend.service.exception;

public class POOQuantityNotFoundException extends ResourceNotFoundException {
    public POOQuantityNotFoundException(Long productOptionOptionQuantityId) {
        super("ProductOptionOptionQuantity with ID[" + productOptionOptionQuantityId + "] not found");
    }
}
