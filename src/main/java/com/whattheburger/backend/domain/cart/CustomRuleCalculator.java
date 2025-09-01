package com.whattheburger.backend.domain.cart;

import com.whattheburger.backend.service.dto.cart.calculator.CustomRuleCalculationDetail;
import com.whattheburger.backend.service.dto.cart.calculator.CustomRuleCalculationResult;
import com.whattheburger.backend.service.dto.cart.calculator.CustomRuleCalculatorDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class CustomRuleCalculator implements PriceCalculator<List<CustomRuleCalculatorDto>, CustomRuleCalculationResult>{
    @Override
    public CustomRuleCalculationResult calculateTotalPrice(List<CustomRuleCalculatorDto> customRuleCalculatorDtos) {
        List<CustomRuleCalculationDetail> customRuleCalculationDetails = customRuleCalculatorDtos.stream()
                .map(customRuleCalculatorDto -> calculatePrice(customRuleCalculatorDto))
                .toList();
        BigDecimal customRuleTotalPrice = customRuleCalculationDetails.stream()
                .map(CustomRuleCalculationDetail::getCalculatedCustomRulePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CustomRuleCalculationResult(
                customRuleCalculationDetails,
                customRuleTotalPrice
        );
    }

    private CustomRuleCalculationDetail calculatePrice(CustomRuleCalculatorDto customRuleCalculatorDto) {
        return new CustomRuleCalculationDetail(
                customRuleCalculatorDto.getCustomRuleId(),
                customRuleCalculatorDto.getOptionCalculationResult().getOptionCalculationDetails(),
                customRuleCalculatorDto.getOptionCalculationResult().getOptionTotalPrice()
        );
    }
}
