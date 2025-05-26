package com.whattheburger.backend.service.exception;

import com.whattheburger.backend.exception.ApiException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class OptionNotFoundException extends ApiException {

    public OptionNotFoundException(Long optionId) {
        super("Option with ID[" + optionId + "] not found", HttpStatus.NOT_FOUND);
    }
}