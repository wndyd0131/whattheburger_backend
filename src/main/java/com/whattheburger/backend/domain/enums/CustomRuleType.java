package com.whattheburger.backend.domain.enums;

public enum CustomRuleType {
    /**
     * UNIQUE: Choose one
     * LIMIT: Limit from min_selection ~ max_selection
     * FREE: Choose from 0 ~
     */
    UNIQUE, LIMIT, FREE
}
