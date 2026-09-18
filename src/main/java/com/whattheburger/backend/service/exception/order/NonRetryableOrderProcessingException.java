package com.whattheburger.backend.service.exception.order;

public class NonRetryableOrderProcessingException extends RuntimeException {
    public NonRetryableOrderProcessingException() {
        super("Order processing failed and should not be retried");
    }

    public NonRetryableOrderProcessingException(Throwable cause) {
        super("Order processing failed and should not be retried", cause);
    }
}
