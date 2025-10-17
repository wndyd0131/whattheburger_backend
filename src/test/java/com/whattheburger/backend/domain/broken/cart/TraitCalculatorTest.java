package com.whattheburger.backend.domain.broken.cart;

import com.whattheburger.backend.domain.cart.TraitCalculator;
import com.whattheburger.backend.domain.cart.exception.TraitCalcStrategyNotSupportedException;
import com.whattheburger.backend.domain.cart.strategy.BinaryStrategy;
import com.whattheburger.backend.domain.cart.strategy.TraitCalcStrategyResolver;
import com.whattheburger.backend.domain.enums.OptionTraitType;
import com.whattheburger.backend.service.dto.cart.calculator.TraitCalculationResult;
import com.whattheburger.backend.service.dto.cart.calculator.TraitCalculatorDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TraitCalculatorTest {

    @Mock
    TraitCalcStrategyResolver strategyResolver;
    @InjectMocks
    TraitCalculator traitCalculator;

    @Test
    public void givenValidTraitDetails_whenOptionTypeIsBinary_thenReturnExpectedPrice() {

        BinaryStrategy mockStrategy = new BinaryStrategy();

        List<TraitCalculatorDto> traitCalculatorDtos = List.of(
                new TraitCalculatorDto(
                        1L,
                        BigDecimal.valueOf(1.99),
                        0,
                        1,
                        OptionTraitType.BINARY
                ),
                new TraitCalculatorDto(
                        1L,
                        BigDecimal.valueOf(3.99),
                        0,
                        1,
                        OptionTraitType.BINARY
                )
        );
        when(strategyResolver.resolve(any(TraitCalculatorDto.class))).thenReturn(mockStrategy);
        TraitCalculationResult traitCalculationResult = traitCalculator.calculateTotalPrice(traitCalculatorDtos);
        Assertions.assertEquals(BigDecimal.valueOf(5.98), traitCalculationResult.getTraitTotalPrice());
    }

    @Test
    public void givenValidTraitDetails_whenOptionTypeIsNotSupported_thenThrowException() {
        List<TraitCalculatorDto> traitCalculatorDtos = List.of(
                new TraitCalculatorDto(
                        1L,
                        BigDecimal.valueOf(1.99),
                        0,
                        1,
                        null
                ),
                new TraitCalculatorDto(
                        1L,
                        BigDecimal.valueOf(3.99),
                        0,
                        1,
                        null
                )
        );
        when(strategyResolver.resolve(any(TraitCalculatorDto.class))).thenThrow(new TraitCalcStrategyNotSupportedException());

        Assertions.assertThrows(
                TraitCalcStrategyNotSupportedException.class,
                () -> traitCalculator.calculateTotalPrice(traitCalculatorDtos)
        );
    }

}
