package com.whattheburger.backend.service.exception;

import com.whattheburger.backend.exception.ApiException;
import org.springframework.http.HttpStatus;

public class ProductOptionTraitNotFoundException extends ResourceNotFoundException {
    public ProductOptionTraitNotFoundException(Long productOptionTraitId) {
        super("ProductOption with ID " + productOptionTraitId + " not found");
    }
}
