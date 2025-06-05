package com.whattheburger.backend.service.exception;

import com.whattheburger.backend.exception.ApiException;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;


public class ProductOptionNotFoundException extends ResourceNotFoundException {
    public ProductOptionNotFoundException(Long productOptionId) {
        super("ProductOption with ID " + productOptionId + " not found");
    }
}
