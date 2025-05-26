package com.whattheburger.backend.service.exception;

import com.whattheburger.backend.exception.ApiException;
import org.springframework.http.HttpStatus;

public class ProductOptionTraitNotFoundException extends ApiException {
    public ProductOptionTraitNotFoundException(Long productOptionId, Long optionTraitId) {
        super("ProductOptionTrait with ID[" + productOptionId + ", " + optionTraitId + "] not found", HttpStatus.NOT_FOUND);

    }
}
