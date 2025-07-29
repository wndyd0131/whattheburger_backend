package com.whattheburger.backend.domain.cart;

import com.whattheburger.backend.domain.cart.strategy.TraitCalcStrategyResolver;
import com.whattheburger.backend.service.dto.cart.calculator.TraitCalcDetail;
import com.whattheburger.backend.domain.cart.strategy.TraitCalcStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TraitCalculator implements PriceCalculator<List<TraitCalcDetail>> {
    private final TraitCalcStrategyResolver strategyResolver;

    @Override
    public BigDecimal calculateTotalPrice(List<TraitCalcDetail> traitCalcDetails) {
        BigDecimal traitTotalPrice = traitCalcDetails.stream()
                .peek(trait -> log.info("trait exist {}", trait))
                .map(this::calculatePrice)
                .peek(value -> log.info("trait value {}", value))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return traitTotalPrice;
    }

//    private boolean shouldIncludeForPricing(TraitDetail trait) {
//        if (trait.getOptionTraitType() == OptionTraitType.BINARY) {
//            return trait.getRequestedSelection() != 0;
//        }
//        if (trait.getRequestedSelection() == trait.getDefaultSelection())
//            return false;
//        return true;
//    }

    public BigDecimal calculatePrice(TraitCalcDetail trait) {
        TraitCalcStrategy strategy = strategyResolver.resolve(trait);
        BigDecimal price = strategy.execute(trait);
        return price;
    }
}
