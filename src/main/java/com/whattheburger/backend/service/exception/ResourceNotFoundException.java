package com.whattheburger.backend.service.exception;

import com.whattheburger.backend.exception.ApiException;
import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String msg) {
        super(msg, HttpStatus.NOT_FOUND);
    }
}
