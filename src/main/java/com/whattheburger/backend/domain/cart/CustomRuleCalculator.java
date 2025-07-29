package com.whattheburger.backend.domain.cart;

import com.whattheburger.backend.service.dto.cart.calculator.CustomRuleCalcDetail;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class CustomRuleCalculator implements PriceCalculator<List<CustomRuleCalcDetail>>{
    @Override
    public BigDecimal calculateTotalPrice(List<CustomRuleCalcDetail> details) {
        BigDecimal totalPrice = details.stream()
                .map(this::calculatePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return totalPrice;
    }

    private BigDecimal calculatePrice(CustomRuleCalcDetail detail) {
        return detail.getOptionTotalPrice();
    }
}
