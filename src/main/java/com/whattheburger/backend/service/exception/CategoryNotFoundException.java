package com.whattheburger.backend.service.exception;

import com.whattheburger.backend.exception.ApiException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CategoryNotFoundException extends ApiException {
    public CategoryNotFoundException(Long categoryId) {
        super("Category with ID[" + categoryId + "] not found", HttpStatus.NOT_FOUND);
    }
}
