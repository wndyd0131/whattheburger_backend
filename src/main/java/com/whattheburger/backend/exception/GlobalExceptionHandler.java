package com.whattheburger.backend.exception;

import com.whattheburger.backend.security.exception.AuthenticationCredentialsNotFoundException;
import com.whattheburger.backend.security.exception.UserPrincipalNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserPrincipalNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserPrincipalNotFoundException(UserPrincipalNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), ex.getStatus().value());
        return new ResponseEntity<>(
                errorResponse,
                ex.getStatus()
        );
    }
    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationCredentialsNotFoundException(AuthenticationCredentialsNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), ex.getStatus().value());
        return new ResponseEntity<>(
                errorResponse,
                ex.getStatus()
        );
    }
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
        ex.printStackTrace();
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), ex.getStatus().value());
        return new ResponseEntity<>(
                errorResponse,
                ex.getStatus()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAnyExceptions(Exception ex) {
        ex.printStackTrace();
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(
                errorResponse,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

}
