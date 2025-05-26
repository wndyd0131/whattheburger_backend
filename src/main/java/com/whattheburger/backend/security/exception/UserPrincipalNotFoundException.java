package com.whattheburger.backend.security.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class UserPrincipalNotFoundException extends RuntimeException {
    private final HttpStatus status;
    public UserPrincipalNotFoundException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
