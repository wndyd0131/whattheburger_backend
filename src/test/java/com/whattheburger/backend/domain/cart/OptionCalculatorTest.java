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
                        .price(4.99D)
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
                        .price(7.99D)
                        .defaultQuantity(2)
                        .isDefault(true)
                        .build()
        );
        when(traitCalculator.calculateTotalPrice(any(List.class))).thenReturn(0D);
        Double totalPrice = optionCalculator.calculateTotalPrice(mockOptionDetails);
        Assertions.assertEquals(27.95, totalPrice);
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
                                        .price(1D)
                                        .requestedId(1L)
                                        .defaultId(1L)
                                        .build()
                        )
                        .quantity(5)
                        .price(4.99D)
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
                                        .price(2D)
                                        .requestedId(1L)
                                        .defaultId(1L)
                                        .build()
                        )
                        .quantity(3) // fake
                        .price(7.99D)
                        .defaultQuantity(2)
                        .isDefault(true)
                        .build()
        );
        when(traitCalculator.calculateTotalPrice(any(List.class))).thenReturn(0D);
        Double totalPrice = optionCalculator.calculateTotalPrice(mockOptionDetails);
        Assertions.assertEquals(27.95, totalPrice);
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
                        .quantityDetail(
                                QuantityDetail
                                        .builder()
                                        .price(1D)
                                        .requestedId(1L)
                                        .defaultId(1L)
                                        .build()
                        )
                        .quantity(5)
                        .price(4.99D)
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
                                        .price(2D)
                                        .requestedId(1L)
                                        .defaultId(1L)
                                        .build()
                        )
                        .quantity(3) // fake
                        .price(7.99D)
                        .defaultQuantity(2)
                        .isDefault(true)
                        .build()
        );
        when(traitCalculator.calculateTotalPrice(any(List.class))).thenReturn(0D);
        Double totalPrice = optionCalculator.calculateTotalPrice(mockOptionDetails);
        Assertions.assertEquals(27.95, totalPrice);
    }
}
