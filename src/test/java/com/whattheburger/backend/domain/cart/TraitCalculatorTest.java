package com.whattheburger.backend.domain.cart;

import com.whattheburger.backend.domain.cart.exception.TraitCalcStrategyNotSupportedException;
import com.whattheburger.backend.domain.cart.strategy.BinaryStrategy;
import com.whattheburger.backend.domain.cart.strategy.TraitCalcStrategyResolver;
import com.whattheburger.backend.domain.enums.OptionTraitType;
import com.whattheburger.backend.service.dto.cart.calculator.TraitDetail;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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

        List<TraitDetail> traitDetails = List.of(
                new TraitDetail(
                        BigDecimal.valueOf(1.99),
                        0,
                        1,
                        OptionTraitType.BINARY
                ),
                new TraitDetail(
                        BigDecimal.valueOf(3.99),
                        0,
                        1,
                        OptionTraitType.BINARY
                )
        );
        when(strategyResolver.resolve(any(TraitDetail.class))).thenReturn(mockStrategy);
        BigDecimal totalPrice = traitCalculator.calculateTotalPrice(traitDetails);
        Assertions.assertEquals(BigDecimal.valueOf(5.98), totalPrice);
    }

    @Test
    public void givenValidTraitDetails_whenOptionTypeIsNotSupported_thenThrowException() {
        List<TraitDetail> traitDetails = List.of(
                new TraitDetail(
                        BigDecimal.valueOf(1.99),
                        0,
                        1,
                        null
                ),
                new TraitDetail(
                        BigDecimal.valueOf(3.99),
                        0,
                        1,
                        null
                )
        );
        when(strategyResolver.resolve(any(TraitDetail.class))).thenThrow(new TraitCalcStrategyNotSupportedException());

        Assertions.assertThrows(
                TraitCalcStrategyNotSupportedException.class,
                () -> traitCalculator.calculateTotalPrice(traitDetails)
        );
    }

}
