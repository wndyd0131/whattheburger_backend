package com.whattheburger.backend.domain.cart;

import com.whattheburger.backend.service.dto.cart.calculator.PriceCalculationResult;

import java.math.BigDecimal;

public interface PriceCalculator<T, R extends PriceCalculationResult> {
    R calculateTotalPrice(T detail);
}
