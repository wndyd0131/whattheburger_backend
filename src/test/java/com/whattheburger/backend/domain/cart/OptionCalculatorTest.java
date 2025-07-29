package com.whattheburger.backend.domain.cart;

import com.whattheburger.backend.service.dto.cart.calculator.OptionCalcDetail;
import com.whattheburger.backend.service.dto.cart.calculator.QuantityCalcDetail;
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
public class OptionCalculatorTest {

    OptionCalculator optionCalculator = new OptionCalculator();

    @Test
    public void givenCountableOptionDetail_whenCalculate_thenReturnExpectedPrice() {
        List<OptionCalcDetail> mockOptionCalcDetails = List.of(
                OptionCalcDetail
                        .builder()
                        .productOptionId(1L)
                        .isSelected(true)
                        .traitCalcDetails(
                                List.of()
                        )
                        .quantityCalcDetail(null)
                        .quantity(5)
                        .traitTotalPrice(BigDecimal.ZERO)
                        .price(new BigDecimal("4.99"))
                        .defaultQuantity(1)
                        .isDefault(true)
                        .build(), // 19.96 + 0 = 19.96
                OptionCalcDetail
                        .builder()
                        .productOptionId(2L)
                        .isSelected(true)
                        .traitCalcDetails(
                                List.of()
                        )
                        .quantityCalcDetail(null)
                        .quantity(3)
                        .price(new BigDecimal("7.99"))
                        .defaultQuantity(2)
                        .isDefault(true)
                        .traitTotalPrice(new BigDecimal("1.97"))
                        .build() // 7.99 + 1.97 = 9.96
        );

        BigDecimal totalPrice = optionCalculator.calculateTotalPrice(mockOptionCalcDetails);
        Assertions.assertEquals(BigDecimal.valueOf(29.92), totalPrice); // 19.96 + 9.96 =
    }

    @Test
    public void givenUncountableOptionDetail_whenCalculate_thenReturnExpectedPrice() {
        List<OptionCalcDetail> mockOptionCalcDetails = List.of(
                OptionCalcDetail
                        .builder()
                        .isSelected(true)
                        .traitCalcDetails(
                                List.of()
                        )
                        .quantityCalcDetail(
                                QuantityCalcDetail
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
                        .traitTotalPrice(BigDecimal.ZERO)
                        .build(), // basePrice(0) + quantityPrice(0) + traitPrice(0) = 0
                OptionCalcDetail
                        .builder()
                        .isSelected(true)
                        .traitCalcDetails(
                                List.of()
                        )
                        .quantityCalcDetail(
                                QuantityCalcDetail
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
                        .traitTotalPrice(new BigDecimal("1.99"))
                        .build() // basePrice(0) + quantityPrice(1.19) + traitPrice(1.99) = 3.18
        );
        BigDecimal totalPrice = optionCalculator.calculateTotalPrice(mockOptionCalcDetails);
        Assertions.assertEquals(new BigDecimal("3.18"), totalPrice); // 0 + 3.18 = 3.18
    }

    @Test
    public void givenCountableAndUncountableOptionDetail_whenCalculate_thenReturnExpectedPrice() {
        List<OptionCalcDetail> mockOptionCalcDetails = List.of(
                OptionCalcDetail // Large bun
                        .builder()
                        .isSelected(true)
                        .traitCalcDetails(
                                List.of()
                        )
                        .quantityCalcDetail(null)
                        .quantity(7)
                        .price(BigDecimal.valueOf(4.99))
                        .defaultQuantity(1)
                        .isDefault(false)
                        .traitTotalPrice(BigDecimal.ZERO)
                        .build(), // quantity(7) * basePrice(4.99) + traitPrice(0) = 34.93
                OptionCalcDetail // Small bun
                        .builder()
                        .isSelected(true)
                        .traitCalcDetails(
                                List.of()
                        )
                        .quantityCalcDetail(
                                QuantityCalcDetail
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
                        .traitTotalPrice(BigDecimal.ZERO)
                        .build() // quantity(2.99) + basePrice(3.99) = 6.98
        );
        BigDecimal totalPrice = optionCalculator.calculateTotalPrice(mockOptionCalcDetails);
        Assertions.assertEquals(BigDecimal.valueOf(41.91), totalPrice); // 34.93 + 6.98 = 41.91
    }
}
