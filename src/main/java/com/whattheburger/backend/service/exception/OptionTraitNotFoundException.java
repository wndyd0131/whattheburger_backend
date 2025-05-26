package com.whattheburger.backend.service.exception;

import com.whattheburger.backend.exception.ApiException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class OptionTraitNotFoundException extends ApiException {

    public OptionTraitNotFoundException(Long optionTraitId) {
        super("OptionTrait with ID[" + optionTraitId + "] not found", HttpStatus.NOT_FOUND);
    }
}