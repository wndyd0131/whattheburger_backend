package com.whattheburger.backend.domain.cart.strategy;

import com.whattheburger.backend.domain.enums.OptionTraitType;
import com.whattheburger.backend.service.dto.cart.calculator.TraitCalculatorDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class BinaryStrategy implements TraitCalcStrategy {
    @Override
    public OptionTraitType getSupportedType() {
        return OptionTraitType.BINARY;
    }

    @Override
    public BigDecimal execute(TraitCalculatorDto traitCalculatorDto) {
        log.info("Binary");
        if (traitCalculatorDto.getRequestedSelection() != 0) {
            log.info("Trait price: {}", traitCalculatorDto.getPrice());
            return traitCalculatorDto.getPrice();
        }
        return BigDecimal.ZERO;
    }
}
