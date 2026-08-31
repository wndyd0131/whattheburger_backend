package com.whattheburger.backend.service.exception.cart;

import com.whattheburger.backend.service.exception.BadRequestException;

public class InvalidOptionRequestException extends BadRequestException {
    public InvalidOptionRequestException(String message) {
        super(message);
    }

    public static InvalidOptionRequestException missingCountableQuantity(Long productOptionId, Integer optionQuantity) {
        return new InvalidOptionRequestException(String.format(
                "Selected COUNTABLE productOption [%d] requires a positive optionQuantity but was [%s]",
                productOptionId,
                optionQuantity));
    }

    public static InvalidOptionRequestException missingQuantityDetail(Long productOptionId) {
        return new InvalidOptionRequestException(String.format(
                "Selected UNCOUNTABLE productOption [%d] requires a quantityDetailRequest",
                productOptionId));
    }
}
