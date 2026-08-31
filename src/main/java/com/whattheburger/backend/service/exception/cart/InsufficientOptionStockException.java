package com.whattheburger.backend.service.exception.cart;

import com.whattheburger.backend.service.exception.BadRequestException;

public class InsufficientOptionStockException extends BadRequestException {
    public InsufficientOptionStockException(Long productOptionId, Long ingredientId, int needed, int currentStock) {
        super(String.format(
                "Insufficient stock for productOption [%d] ingredient [%d]: needed %d, available %d",
                productOptionId,
                ingredientId,
                needed,
                currentStock));
    }
}
