package com.whataburger.whataburgerproject.exception;

public class UserPrincipalNotFoundException extends RuntimeException {
    public UserPrincipalNotFoundException(String message) {
        super(message);
    }
}
