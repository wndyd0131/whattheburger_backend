package com.whataburger.whataburgerproject.exception;

public class CustomException extends RuntimeException {
    private final int status;

    public CustomException(String message, int status) {
        super(message);
        this.status = status;
    }

    public int getHttpStatus() {
        return status;
    }
}

