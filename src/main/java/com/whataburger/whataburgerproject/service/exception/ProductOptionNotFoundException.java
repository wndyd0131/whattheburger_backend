package com.whataburger.whataburgerproject.service.exception;

import com.whataburger.whataburgerproject.exception.ApiException;
import org.springframework.http.HttpStatus;

public class ProductOptionNotFoundException extends ApiException {
    public ProductOptionNotFoundException(Long productId, Long optionId) {
        super("ProductOption with ID[" + productId + ", " + optionId + "] not found", HttpStatus.NOT_FOUND);
    }
}
