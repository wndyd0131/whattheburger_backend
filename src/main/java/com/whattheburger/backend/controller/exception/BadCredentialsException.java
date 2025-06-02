package com.whattheburger.backend.controller.exception;

import com.whattheburger.backend.exception.ApiException;
import org.springframework.http.HttpStatus;

public class BadCredentialsException extends ApiException {
    public BadCredentialsException() {
        super("Invalid credentials", HttpStatus.UNAUTHORIZED);
    }
}
