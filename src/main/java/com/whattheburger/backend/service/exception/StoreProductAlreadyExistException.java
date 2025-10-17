package com.whattheburger.backend.service.exception;

import com.whattheburger.backend.exception.ApiException;
import org.springframework.http.HttpStatus;

import java.util.List;

public class StoreProductAlreadyExistException extends ApiException {
    public StoreProductAlreadyExistException(List<Long> duplicateStoreIds, Long productId) {
        super(
                "Store with ids [" + duplicateStoreIds.toString() + "] already has Product with id [" + productId + "]",
                HttpStatus.BAD_REQUEST
        );
    }
}
