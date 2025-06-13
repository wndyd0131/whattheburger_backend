package com.whattheburger.backend.service.exception;

public class CustomRuleNotFoundException extends ResourceNotFoundException {
    public CustomRuleNotFoundException(Long customRuleId) {
        super("CustomRule with ID " + customRuleId + " not found");
    }
}
