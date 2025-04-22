package com.whataburger.whataburgerproject.service.exception;

import com.whataburger.whataburgerproject.exception.ApiException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CategoryNotFoundException extends ApiException {
    public CategoryNotFoundException(Long categoryId) {
        super("Category with ID[" + categoryId + "] not found", HttpStatus.NOT_FOUND);
    }
}
