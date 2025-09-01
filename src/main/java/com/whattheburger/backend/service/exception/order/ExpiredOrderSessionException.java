package com.whattheburger.backend.service.exception.order;

import com.whattheburger.backend.service.exception.ExpiredSessionException;

public class ExpiredOrderSessionException extends ExpiredSessionException {
    public ExpiredOrderSessionException() {
        super("Order session has been expired");
    }
}
