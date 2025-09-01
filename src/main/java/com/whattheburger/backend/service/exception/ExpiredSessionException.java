package com.whattheburger.backend.service.exception;

import com.whattheburger.backend.exception.ApiException;
import org.springframework.http.HttpStatus;

public class ExpiredSessionException extends ApiException {
    public ExpiredSessionException(String message) { // Resource Session
        super(message, HttpStatus.NOT_FOUND);
    }
}
