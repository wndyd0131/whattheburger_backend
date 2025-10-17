package com.whattheburger.backend.domain.cart;

import com.whattheburger.backend.domain.cart.strategy.TraitCalcStrategyResolver;
import com.whattheburger.backend.service.dto.cart.calculator.TraitCalculationDetail;
import com.whattheburger.backend.service.dto.cart.calculator.TraitCalculationResult;
import com.whattheburger.backend.service.dto.cart.calculator.TraitCalculatorDto;
import com.whattheburger.backend.domain.cart.strategy.TraitCalcStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TraitCalculator implements PriceCalculator<List<TraitCalculatorDto>, TraitCalculationResult> {
    private final TraitCalcStrategyResolver strategyResolver;

    @Override
    public TraitCalculationResult calculateTotalPrice(List<TraitCalculatorDto> traitCalculatorDtos) {
        List<TraitCalculationDetail> traitCalculationDetails = traitCalculatorDtos.stream()
                .map(traitCalculatorDto -> {
                    BigDecimal price = calculatePrice(traitCalculatorDto);
                    return new TraitCalculationDetail(
                            traitCalculatorDto.getProductOptionTraitId(),
                            price
                    );
                })
                .toList();

        BigDecimal traitTotalPrice = traitCalculationDetails.stream()
                .map(TraitCalculationDetail::getCalculatedTraitPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new TraitCalculationResult(
                traitCalculationDetails,
                traitTotalPrice
        );
    }

    public BigDecimal calculatePrice(TraitCalculatorDto traitDto) {
        TraitCalcStrategy strategy = strategyResolver.resolve(traitDto);
        BigDecimal price = strategy.execute(traitDto);
        return price;
    }
}
