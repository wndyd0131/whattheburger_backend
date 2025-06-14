package com.whattheburger.backend.service.exception;

import com.whattheburger.backend.exception.ApiException;
import org.springframework.http.HttpStatus;

public class BadRequestException extends ApiException {
    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
