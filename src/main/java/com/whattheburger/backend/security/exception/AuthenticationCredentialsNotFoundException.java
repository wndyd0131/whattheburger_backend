package com.whattheburger.backend.security.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;

@Getter
public class AuthenticationCredentialsNotFoundException extends AuthenticationException {
    public AuthenticationCredentialsNotFoundException(String message) {
        super(message);
    }
}
