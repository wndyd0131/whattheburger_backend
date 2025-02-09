package com.whataburger.whataburgerproject.service.exception;

import com.whataburger.whataburgerproject.exception.ResourceNotFoundException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CategoryNotFoundException extends ResourceNotFoundException {
    public CategoryNotFoundException(Long categoryId) {
        super("Category", categoryId, HttpStatus.NOT_FOUND);
    }
}
