package com.whattheburger.backend.service.exception;

public class OptionQuantityNotFoundException extends ResourceNotFoundException{
    public OptionQuantityNotFoundException(Long quantityId) {
        super("Quantity with ID[" + quantityId + "] not found");
    }
}
