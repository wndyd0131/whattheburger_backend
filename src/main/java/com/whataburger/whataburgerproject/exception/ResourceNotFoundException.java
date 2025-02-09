package com.whataburger.whataburgerproject.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ResourceNotFoundException extends RuntimeException {
    private final HttpStatus status;

    public ResourceNotFoundException(String resourceName, Long resourceId, HttpStatus status) {
        super(resourceName + " with ID[" + resourceId + "] not found");
        this.status = status;
    }
}
