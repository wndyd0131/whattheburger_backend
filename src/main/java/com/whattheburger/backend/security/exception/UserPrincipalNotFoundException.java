package com.whattheburger.backend.security.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class UserPrincipalNotFoundException extends RuntimeException {
    public UserPrincipalNotFoundException(String message) {
        super(message);
    }
}
