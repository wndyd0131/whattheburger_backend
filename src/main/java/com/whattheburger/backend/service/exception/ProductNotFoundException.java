package com.whattheburger.backend.service.exception;

import com.whattheburger.backend.exception.ApiException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ProductNotFoundException extends ResourceNotFoundException {

    public ProductNotFoundException(Long resourceId) {
        super("Product with ID[" + resourceId + "] not found");
    }
}
