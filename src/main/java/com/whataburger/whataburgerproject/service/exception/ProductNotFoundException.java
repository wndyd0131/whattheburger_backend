package com.whataburger.whataburgerproject.service.exception;

import com.whataburger.whataburgerproject.exception.ResourceNotFoundException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ProductNotFoundException extends ResourceNotFoundException {
    public ProductNotFoundException(Long productId) {
        super("Product", productId, HttpStatus.NOT_FOUND);
    }
}
