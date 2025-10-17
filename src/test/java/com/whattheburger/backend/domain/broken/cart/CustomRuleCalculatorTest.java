package com.whattheburger.backend.domain.broken.cart;

import com.whattheburger.backend.domain.cart.CustomRuleCalculator;
import com.whattheburger.backend.service.dto.cart.calculator.CustomRuleCalculationResult;
import com.whattheburger.backend.service.dto.cart.calculator.CustomRuleCalculatorDto;
import com.whattheburger.backend.service.dto.cart.calculator.OptionCalculationDetail;
import com.whattheburger.backend.service.dto.cart.calculator.OptionCalculationResult;
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
                        .optionCalculationResult(
                                OptionCalculationResult
                                        .builder()
                                        .optionCalculationDetails(List.of())
                                        .optionTotalPrice(new BigDecimal("5.99"))
                                        .build()
                        )
                        .build(),
                CustomRuleCalculatorDto
                        .builder()
                        .customRuleId(2L)
                        .optionCalculationResult(
                                OptionCalculationResult
                                        .builder()
                                        .optionCalculationDetails(List.of())
                                        .optionTotalPrice(new BigDecimal("7.99"))
                                        .build()
                        )
                        .build(),
                CustomRuleCalculatorDto
                        .builder()
                        .customRuleId(3L)
                        .optionCalculationResult(
                                OptionCalculationResult
                                        .builder()
                                        .optionCalculationDetails(List.of())
                                        .optionTotalPrice(new BigDecimal("1.99"))
                                        .build()
                        )
                        .build()
        );

        CustomRuleCalculationResult customRuleCalculationResult = customRuleCalculator.calculateTotalPrice(customRuleCalculatorDtos);
        Assertions.assertEquals(new BigDecimal("15.97"), customRuleCalculationResult.getCustomRuleTotalPrice());
    }
}
