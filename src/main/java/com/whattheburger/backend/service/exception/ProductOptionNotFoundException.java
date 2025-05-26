package com.whattheburger.backend.service.exception;

import com.whattheburger.backend.exception.ApiException;
import org.springframework.http.HttpStatus;

public class ProductOptionNotFoundException extends ApiException {
    public ProductOptionNotFoundException(Long productId, Long optionId) {
        super("ProductOption with ID[" + productId + ", " + optionId + "] not found", HttpStatus.NOT_FOUND);
    }
}
