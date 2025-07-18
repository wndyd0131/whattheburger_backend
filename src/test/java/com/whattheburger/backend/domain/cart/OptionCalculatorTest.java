package com.whattheburger.backend.domain.cart;

import com.whattheburger.backend.domain.enums.OptionTraitType;
import com.whattheburger.backend.service.dto.cart.calculator.OptionDetail;
import com.whattheburger.backend.service.dto.cart.calculator.QuantityDetail;
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
public class OptionCalculatorTest {

    @Mock
    TraitCalculator traitCalculator;

    @InjectMocks
    OptionCalculator optionCalculator;

    @Test
    public void givenCountableOptionDetail_whenCalculate_thenReturnExpectedPrice() {
        List<OptionDetail> mockOptionDetails = List.of(
                OptionDetail // Large bun
                        .builder()
                        .isSelected(true)
                        .traitDetails(
                                List.of()
                        )
                        .quantityDetail(null)
                        .quantity(5)
                        .price(new BigDecimal("4.99"))
                        .defaultQuantity(1)
                        .isDefault(true)
                        .build(),
                OptionDetail // Small bun
                        .builder()
                        .isSelected(true)
                        .traitDetails(
                                List.of()
                        )
                        .quantityDetail(null)
                        .quantity(3)
                        .price(new BigDecimal("7.99"))
                        .defaultQuantity(2)
                        .isDefault(true)
                        .build()
        );
//        when(traitCalculator.calculateTotalPrice(any(List.class))).thenReturn(BigDecimal.ZERO);
        BigDecimal totalPrice = optionCalculator.calculateTotalPrice(mockOptionDetails);
        Assertions.assertEquals(BigDecimal.valueOf(27.95), totalPrice); // 4 * 4.99 + 1 * 7.99 = 27.95
    }

    @Test
    public void givenUncountableOptionDetail_whenCalculate_thenReturnExpectedPrice() {
        List<OptionDetail> mockOptionDetails = List.of(
                OptionDetail // Large bun
                        .builder()
                        .isSelected(true)
                        .traitDetails(
                                List.of()
                        )
                        .quantityDetail(
                                QuantityDetail
                                        .builder()
                                        .price(BigDecimal.valueOf(2))
                                        .requestedId(1L)
                                        .defaultId(1L)
                                        .build()
                        )
                        .quantity(5)
                        .price(BigDecimal.ZERO)
                        .defaultQuantity(1)
                        .isDefault(true)
                        .build(),
                OptionDetail // Small bun
                        .builder()
                        .isSelected(true)
                        .traitDetails(
                                List.of()
                        )
                        .quantityDetail(
                                QuantityDetail
                                        .builder()
                                        .price(BigDecimal.valueOf(1.19))
                                        .requestedId(2L)
                                        .defaultId(1L)
                                        .build()
                        )
                        .quantity(3) // fake
                        .price(BigDecimal.ZERO)
                        .defaultQuantity(2)
                        .isDefault(true)
                        .build()
        );
//        when(traitCalculator.calculateTotalPrice(any(List.class))).thenReturn(BigDecimal.ZERO);
        BigDecimal totalPrice = optionCalculator.calculateTotalPrice(mockOptionDetails);
        Assertions.assertEquals(BigDecimal.valueOf(1.19), totalPrice); // 0 + 1.19 = 1.19
    }

    @Test
    public void givenCountableAndUncountableOptionDetail_whenCalculate_thenReturnExpectedPrice() {
        List<OptionDetail> mockOptionDetails = List.of(
                OptionDetail // Large bun
                        .builder()
                        .isSelected(true)
                        .traitDetails(
                                List.of()
                        )
                        .quantityDetail(null)
                        .quantity(7)
                        .price(BigDecimal.valueOf(4.99))
                        .defaultQuantity(1)
                        .isDefault(false)
                        .build(),
                OptionDetail // Small bun
                        .builder()
                        .isSelected(true)
                        .traitDetails(
                                List.of()
                        )
                        .quantityDetail(
                                QuantityDetail
                                        .builder()
                                        .price(BigDecimal.valueOf(2.99))
                                        .requestedId(2L)
                                        .defaultId(1L)
                                        .build()
                        )
                        .quantity(1) // fake
                        .price(BigDecimal.valueOf(3.99))
                        .defaultQuantity(1)
                        .isDefault(false)
                        .build()
        );
//        when(traitCalculator.calculateTotalPrice(any(List.class))).thenReturn(BigDecimal.ZERO);
        BigDecimal totalPrice = optionCalculator.calculateTotalPrice(mockOptionDetails);
        Assertions.assertEquals(BigDecimal.valueOf(41.91), totalPrice); // 4.99 * 7 + 3.99 + 2.99 = 41.91
    }
}
