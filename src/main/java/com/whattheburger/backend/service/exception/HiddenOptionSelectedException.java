package com.whattheburger.backend.service.exception;

public class HiddenOptionSelectedException extends BadRequestException {
    public HiddenOptionSelectedException(Long productOptionId) {
        super("Hidden product option " + productOptionId + " cannot be selected");
    }
}
