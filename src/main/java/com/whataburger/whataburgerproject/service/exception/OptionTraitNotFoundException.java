package com.whataburger.whataburgerproject.service.exception;

import com.whataburger.whataburgerproject.exception.ApiException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class OptionTraitNotFoundException extends ApiException {

    public OptionTraitNotFoundException(Long optionTraitId) {
        super("OptionTrait with ID[" + optionTraitId + "] not found", HttpStatus.NOT_FOUND);
    }
}