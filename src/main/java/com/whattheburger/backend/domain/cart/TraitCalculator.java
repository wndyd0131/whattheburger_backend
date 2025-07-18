package com.whattheburger.backend.domain.cart;

import com.whattheburger.backend.domain.enums.OptionTraitType;
import com.whattheburger.backend.service.dto.cart.calculator.OptionDetail;
import com.whattheburger.backend.service.dto.cart.calculator.TraitDetail;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
public class TraitCalculator implements PriceCalculator<List<TraitDetail>> {
    @Override
    public BigDecimal calculateTotalPrice(List<TraitDetail> traitDetails) {
        // handle based on is default
        BigDecimal traitTotalPrice = traitDetails.stream()
                .filter(this::shouldIncludeForPricing)
                .map(this::calculatePrice)
                .peek(value -> log.info("trait value {}", value))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return traitTotalPrice;
    }

    private boolean shouldIncludeForPricing(TraitDetail trait) {
        if (trait.getOptionTraitType() == OptionTraitType.BINARY) {
            return trait.getRequestedSelection() != 0;
        }
        if (trait.getRequestedSelection() == trait.getDefaultSelection())
            return false;
        return true;
    }

    private BigDecimal calculatePrice(TraitDetail trait) {
        return trait.getPrice();
    }
}
