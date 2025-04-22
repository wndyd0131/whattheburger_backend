package com.whataburger.whataburgerproject.service.exception;

import com.whataburger.whataburgerproject.exception.ApiException;
import org.springframework.http.HttpStatus;

public class ProductOptionTraitNotFoundException extends ApiException {
    public ProductOptionTraitNotFoundException(Long productOptionId, Long optionTraitId) {
        super("ProductOptionTrait with ID[" + productOptionId + ", " + optionTraitId + "] not found", HttpStatus.NOT_FOUND);

    }
}
