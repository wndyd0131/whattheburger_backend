package com.whattheburger.backend.service.exception;

public class MapboxApiException extends RuntimeException {
    public MapboxApiException(String message) {
        super(message);
    }
}
