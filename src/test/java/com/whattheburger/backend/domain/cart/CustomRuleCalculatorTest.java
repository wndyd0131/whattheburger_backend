package com.whattheburger.backend.domain.cart;

import com.whattheburger.backend.service.dto.cart.calculator.CustomRuleCalcDetail;
import com.whattheburger.backend.service.dto.cart.calculator.ProductCalcDetail;
import com.whattheburger.backend.utils.MockCalculatorDtoFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

public class CustomRuleCalculatorTest {
    CustomRuleCalculator customRuleCalculator = new CustomRuleCalculator();

    @Test
    public void givenCustomRuleDetails_whenCalculate_thenReturnExpectedPrice() {
        List<CustomRuleCalcDetail> customRuleCalcDetails = List.of(
                CustomRuleCalcDetail
                        .builder()
                        .customRuleId(1L)
                        .optionCalcDetails(List.of())
                        .optionTotalPrice(new BigDecimal("5.99"))
                        .build(),
                CustomRuleCalcDetail
                        .builder()
                        .customRuleId(2L)
                        .optionCalcDetails(List.of())
                        .optionTotalPrice(new BigDecimal("7.99"))
                        .build(),
                CustomRuleCalcDetail
                        .builder()
                        .customRuleId(3L)
                        .optionCalcDetails(List.of())
                        .optionTotalPrice(new BigDecimal("1.99"))
                        .build()
        );

        BigDecimal customRuleTotalPrice = customRuleCalculator.calculateTotalPrice(customRuleCalcDetails);
        Assertions.assertEquals(new BigDecimal("15.97"), customRuleTotalPrice);
    }
}
