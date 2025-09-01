package com.whattheburger.backend.domain.cart.strategy;

import com.whattheburger.backend.domain.enums.OptionTraitType;
import com.whattheburger.backend.service.dto.cart.calculator.TraitCalculatorDto;

import java.math.BigDecimal;

public interface TraitCalcStrategy {
    OptionTraitType getSupportedType();
    BigDecimal execute(TraitCalculatorDto traitCalculatorDto);
}
