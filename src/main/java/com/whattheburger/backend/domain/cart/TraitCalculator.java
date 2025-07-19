package com.whattheburger.backend.domain.cart;

import com.whattheburger.backend.domain.cart.exception.TraitCalcStrategyNotSupportedException;
import com.whattheburger.backend.domain.cart.strategy.TraitCalcStrategyResolver;
import com.whattheburger.backend.domain.enums.OptionTraitType;
import com.whattheburger.backend.service.dto.cart.calculator.TraitDetail;
import com.whattheburger.backend.domain.cart.strategy.BinaryStrategy;
import com.whattheburger.backend.domain.cart.strategy.TraitCalcStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TraitCalculator implements PriceCalculator<List<TraitDetail>> {
    private final TraitCalcStrategyResolver strategyResolver;

    @Override
    public BigDecimal calculateTotalPrice(List<TraitDetail> traitDetails) {
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
        TraitCalcStrategy strategy = strategyResolver.resolve(trait);
        BigDecimal price = strategy.execute(trait);
        return price;
    }
}
