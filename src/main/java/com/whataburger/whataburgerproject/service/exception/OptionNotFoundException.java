package com.whataburger.whataburgerproject.service.exception;

import com.whataburger.whataburgerproject.exception.ResourceNotFoundException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class OptionNotFoundException extends ResourceNotFoundException {
    public OptionNotFoundException(Long optionId) {
        super("Option", optionId, HttpStatus.NOT_FOUND);
    }
}
