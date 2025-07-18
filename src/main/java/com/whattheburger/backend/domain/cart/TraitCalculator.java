package com.whattheburger.backend.domain.cart;

import com.whattheburger.backend.domain.enums.OptionTraitType;
import com.whattheburger.backend.service.dto.cart.calculator.OptionDetail;
import com.whattheburger.backend.service.dto.cart.calculator.TraitDetail;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class TraitCalculator implements PriceCalculator<List<TraitDetail>> {
    @Override
    public Double calculateTotalPrice(List<TraitDetail> traitDetails) {
        // handle based on is default
        double traitTotalPrice = traitDetails.stream()
                .filter(this::shouldIncludeForPricing)
                .mapToDouble(this::calculatePrice)
                .peek(value -> log.info("trait value {}", value))
                .sum();
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

    private Double calculatePrice(TraitDetail trait) {
        return trait.getPrice();
    }
}
