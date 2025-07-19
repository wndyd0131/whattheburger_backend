package com.whattheburger.backend.domain.cart.exception;

import com.whattheburger.backend.exception.ApiException;
import org.springframework.http.HttpStatus;

public class TraitCalcStrategyNotSupportedException extends ApiException {
    public TraitCalcStrategyNotSupportedException() {
        super(
                "TraitCalcStrategy is not supported for requested option trait type",
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
