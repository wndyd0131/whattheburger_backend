package com.whataburger.whataburgerproject.service.exception;

import com.whataburger.whataburgerproject.exception.ApiException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class OptionNotFoundException extends ApiException {

    public OptionNotFoundException(Long optionId) {
        super("Option with ID[" + optionId + "] not found", HttpStatus.NOT_FOUND);
    }
}