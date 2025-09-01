package com.whattheburger.backend.domain.cart;

import com.whattheburger.backend.service.dto.cart.calculator.CustomRuleCalculatorDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

public class CustomRuleCalculatorTest {
    CustomRuleCalculator customRuleCalculator = new CustomRuleCalculator();

    @Test
    public void givenCustomRuleDetails_whenCalculate_thenReturnExpectedPrice() {
        List<CustomRuleCalculatorDto> customRuleCalculatorDtos = List.of(
                CustomRuleCalculatorDto
                        .builder()
                        .customRuleId(1L)
                        .optionCalculatorDtos(List.of())
                        .optionTotalPrice(new BigDecimal("5.99"))
                        .build(),
                CustomRuleCalculatorDto
                        .builder()
                        .customRuleId(2L)
                        .optionCalculatorDtos(List.of())
                        .optionTotalPrice(new BigDecimal("7.99"))
                        .build(),
                CustomRuleCalculatorDto
                        .builder()
                        .customRuleId(3L)
                        .optionCalculatorDtos(List.of())
                        .optionTotalPrice(new BigDecimal("1.99"))
                        .build()
        );

        BigDecimal customRuleTotalPrice = customRuleCalculator.calculateTotalPrice(customRuleCalculatorDtos);
        Assertions.assertEquals(new BigDecimal("15.97"), customRuleTotalPrice);
    }
}
