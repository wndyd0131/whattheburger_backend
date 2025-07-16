package com.whattheburger.backend.utils;

import com.whattheburger.backend.domain.CustomRule;
import com.whattheburger.backend.domain.enums.CustomRuleType;

import java.util.ArrayList;

public class MockCustomRuleFactory {
    public static CustomRule createMockCustomRule() {
        return CustomRule
                .builder()
                .id(1L)
                .name("Bread")
                .customRuleType(CustomRuleType.UNIQUE)
                .orderIndex(0)
                .minSelection(1)
                .maxSelection(1)
                .productOptions(new ArrayList<>())
                .build();
    }
}
