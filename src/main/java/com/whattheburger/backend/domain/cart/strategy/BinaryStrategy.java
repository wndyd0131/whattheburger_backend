package com.whattheburger.backend.domain.cart.strategy;

import com.whattheburger.backend.domain.enums.OptionTraitType;
import com.whattheburger.backend.service.dto.cart.calculator.TraitDetail;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class BinaryStrategy implements TraitCalcStrategy {
    @Override
    public OptionTraitType getSupportedType() {
        return OptionTraitType.BINARY;
    }

    @Override
    public BigDecimal execute(TraitDetail traitDetail) {
        if (traitDetail.getRequestedSelection() != 0)
            return traitDetail.getPrice();
        return BigDecimal.ZERO;
    }
}
