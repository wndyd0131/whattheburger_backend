package com.whattheburger.backend.domain.cart.strategy;

import com.whattheburger.backend.domain.cart.exception.TraitCalcStrategyNotSupportedException;
import com.whattheburger.backend.domain.enums.OptionTraitType;
import com.whattheburger.backend.service.dto.cart.calculator.TraitCalculatorDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class TraitCalcStrategyResolver {

    private final Map<OptionTraitType, TraitCalcStrategy> strategyMap;

    @Autowired
    public TraitCalcStrategyResolver(List<TraitCalcStrategy> strategies) { // All Components that implement TraitCalcStrategy will be found and injected automatically
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        TraitCalcStrategy::getSupportedType,
                        Function.identity()
                ));
    }

    public TraitCalcStrategy resolve(TraitCalculatorDto traitCalculatorDto) {
        return Optional.ofNullable(strategyMap.get(traitCalculatorDto.getOptionTraitType()))
                .orElseThrow(() -> new TraitCalcStrategyNotSupportedException());
    }
}
