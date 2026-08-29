package com.whattheburger.backend.utils.broken;

import com.whattheburger.backend.domain.enums.OptionTraitType;
import com.whattheburger.backend.service.dto.cart.calculator.*;

import java.math.BigDecimal;
import java.util.List;

public class MockCalculatorDtoFactory {
    public static List<ProductCalculatorDto> createMockProductCalcDetails() {
        ProductCalculatorDto productCalculatorDto1 = ProductCalculatorDto
                .builder()
                .basePrice(BigDecimal.valueOf(5.49))
                .customRuleCalculationResult(
                        CustomRuleCalculationResult
                                .builder()
                                .customRuleCalculationDetails(List.of())
                                .customRuleTotalPrice(BigDecimal.valueOf(2.99))
                                .build()
                )
                .quantity(3)
                .build(); // 25.44
        ProductCalculatorDto productCalculatorDto2 = ProductCalculatorDto
                .builder()
                .basePrice(BigDecimal.valueOf(9.99))
                .customRuleCalculationResult(
                        CustomRuleCalculationResult
                                .builder()
                                .customRuleCalculationDetails(List.of())
                                .customRuleTotalPrice(BigDecimal.valueOf(5.64))
                                .build()
                )
                .quantity(2) // 31.26
                .build();
        // total: $56.70
        return List.of(
                productCalculatorDto1,
                productCalculatorDto2
        );
    }
}
