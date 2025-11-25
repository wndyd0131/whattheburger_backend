package com.whattheburger.backend.utils;

import com.whattheburger.backend.domain.CustomRule;
import com.whattheburger.backend.domain.ProductOption;
import com.whattheburger.backend.domain.enums.CustomRuleType;

import java.util.ArrayList;
import java.util.List;

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

    public static CustomRule createMockCustomRule(
            Long customRuleId,
            String customRuleName,
            CustomRuleType customRuleType,
            Integer orderIndex,
            Integer minSelection,
            Integer maxSelection
    ) {
        return CustomRule
                .builder()
                .id(customRuleId)
                .name(customRuleName)
                .customRuleType(customRuleType)
                .orderIndex(orderIndex)
                .minSelection(minSelection)
                .maxSelection(maxSelection)
                .productOptions(new ArrayList<>())
                .build();
    }
}
