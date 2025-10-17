package com.whattheburger.backend.service.exception;

import com.whattheburger.backend.exception.ApiException;
import org.springframework.http.HttpStatus;

public class ModificationStrategyNotSupportedException extends ApiException {
    public ModificationStrategyNotSupportedException() {
        super(
                "ModificationStrategy is not supported for requested modify type",
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
