package com.whataburger.whataburgerproject.service.exception;

import com.whataburger.whataburgerproject.exception.ApiException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ProductNotFoundException extends ApiException {

    public ProductNotFoundException(Long resourceId) {
        super("Product with ID[" + resourceId + "] not found", HttpStatus.NOT_FOUND);
    }
}
