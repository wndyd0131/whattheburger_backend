package com.whattheburger.backend.domain.cart;

import com.whattheburger.backend.domain.enums.CountType;
import com.whattheburger.backend.service.dto.cart.calculator.OptionCalculationResult;
import com.whattheburger.backend.service.dto.cart.calculator.OptionCalculatorDto;
import com.whattheburger.backend.service.dto.cart.calculator.QuantityCalculatorDto;
import com.whattheburger.backend.service.dto.cart.calculator.TraitCalculationResult;
import com.whattheburger.backend.service.exception.cart.InvalidOptionRequestException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class OptionCalculatorTest {

    OptionCalculator optionCalculator = new OptionCalculator();

    @Test
    public void givenCountableOptionDetail_whenCalculate_thenReturnExpectedPrice() {
        List<OptionCalculatorDto> mockOptionCalculatorDtos = List.of(
                OptionCalculatorDto
                        .builder()
                        .productOptionId(1L)
                        .isSelected(true)
                        .traitCalculationResult(
                                new TraitCalculationResult(
                                        List.of(),
                                        BigDecimal.ZERO
                                )
                        )
                        .quantityCalculatorDto(null)
                        .quantity(5)
//                        .traitTotalPrice(BigDecimal.ZERO)
                        .price(new BigDecimal("4.99"))
                        .defaultQuantity(1)
                        .isDefault(true)
                        .build(), // 19.96 + 0 = 19.96
                OptionCalculatorDto
                        .builder()
                        .productOptionId(2L)
                        .isSelected(true)
                        .traitCalculationResult(
                                new TraitCalculationResult(
                                        List.of(),
                                        BigDecimal.valueOf(1.97)
                                )
                        )
                        .quantityCalculatorDto(null)
                        .quantity(3)
                        .price(BigDecimal.valueOf(7.99))
                        .defaultQuantity(2)
                        .isDefault(true)
                        .build() // 7.99 + 1.97 = 9.96
        );

        OptionCalculationResult optionCalculationResult = optionCalculator.calculateTotalPrice(mockOptionCalculatorDtos);
        Assertions.assertEquals(BigDecimal.valueOf(29.92), optionCalculationResult.getOptionTotalPrice()); // 19.96 + 9.96 =
    }

    @Test
    public void givenUncountableOptionDetail_whenCalculate_thenReturnExpectedPrice() {
        List<OptionCalculatorDto> mockOptionCalculatorDtos = List.of(
                OptionCalculatorDto
                        .builder()
                        .isSelected(true)
                        .traitCalculationResult(
                                TraitCalculationResult
                                        .builder()
                                        .traitCalculationDetails(List.of())
                                        .traitTotalPrice(BigDecimal.ZERO)
                                        .build()
                        )
                        .quantityCalculatorDto(
                                QuantityCalculatorDto
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
                        .traitCalculationResult(
                                TraitCalculationResult
                                        .builder()
                                        .traitCalculationDetails(List.of())
                                        .traitTotalPrice(BigDecimal.ZERO)
                                        .build()
                        )
                        .build(), // basePrice(0) + quantityPrice(0) + traitPrice(0) = 0
                OptionCalculatorDto
                        .builder()
                        .isSelected(true)
                        .traitCalculationResult(
                                TraitCalculationResult
                                        .builder()
                                        .traitCalculationDetails(List.of())
                                        .traitTotalPrice(BigDecimal.valueOf(1.99))
                                        .build()
                        )
                        .quantityCalculatorDto(
                                QuantityCalculatorDto
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
//                        .traitTotalPrice(new BigDecimal("1.99"))
                        .build() // basePrice(0) + quantityPrice(1.19) + traitPrice(1.99) = 3.18
        );
        OptionCalculationResult optionCalculationResult = optionCalculator.calculateTotalPrice(mockOptionCalculatorDtos);
        Assertions.assertEquals(new BigDecimal("3.18"), optionCalculationResult.getOptionTotalPrice()); // 0 + 3.18 = 3.18
    }

    @Test
    public void givenCountableAndUncountableOptionDetail_whenCalculate_thenReturnExpectedPrice() {
        List<OptionCalculatorDto> mockOptionCalculatorDtos = List.of(
                OptionCalculatorDto // Large bun
                        .builder()
                        .isSelected(true)
                        .traitCalculationResult(
                                TraitCalculationResult
                                        .builder()
                                        .traitCalculationDetails(List.of())
                                        .traitTotalPrice(BigDecimal.ZERO)
                                        .build()
                        )
                        .quantityCalculatorDto(null)
                        .quantity(7)
                        .price(BigDecimal.valueOf(4.99))
                        .defaultQuantity(1)
                        .isDefault(false)
//                        .traitTotalPrice(BigDecimal.ZERO)
                        .build(), // quantity(7) * basePrice(4.99) + traitPrice(0) = 34.93
                OptionCalculatorDto // Small bun
                        .builder()
                        .isSelected(true)
                        .traitCalculationResult(
                                TraitCalculationResult
                                        .builder()
                                        .traitCalculationDetails(List.of())
                                        .traitTotalPrice(BigDecimal.ZERO)
                                        .build()
                        )
                        .quantityCalculatorDto(
                                QuantityCalculatorDto
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
//                        .traitTotalPrice(BigDecimal.ZERO)
                        .build() // quantity(2.99) + basePrice(3.99) = 6.98
        );
        OptionCalculationResult optionCalculationResult = optionCalculator.calculateTotalPrice(mockOptionCalculatorDtos);
        Assertions.assertEquals(BigDecimal.valueOf(41.91), optionCalculationResult.getOptionTotalPrice()); // 34.93 + 6.98 = 41.91
    }

    @Test
    public void givenDefaultNoneOptionWithNullQuantity_whenCalculate_thenReturnTraitPriceOnly() {
        List<OptionCalculatorDto> mockOptionCalculatorDtos = List.of(
                OptionCalculatorDto
                        .builder()
                        .productOptionId(1L)
                        .countType(CountType.NONE)
                        .isSelected(true)
                        .isDefault(true)
                        .quantity(null)
                        .quantityCalculatorDto(null)
                        .price(BigDecimal.valueOf(2.99))
                        .traitCalculationResult(
                                new TraitCalculationResult(
                                        List.of(),
                                        BigDecimal.valueOf(0.50)
                                )
                        )
                        .build()
        );

        OptionCalculationResult result = optionCalculator.calculateTotalPrice(mockOptionCalculatorDtos);

        Assertions.assertEquals(BigDecimal.valueOf(0.50), result.getOptionTotalPrice());
    }

    @Test
    public void givenNonDefaultNoneOptionWithNullQuantity_whenCalculate_thenReturnFlatOptionAndTraitPrice() {
        List<OptionCalculatorDto> mockOptionCalculatorDtos = List.of(
                OptionCalculatorDto
                        .builder()
                        .productOptionId(2L)
                        .countType(CountType.NONE)
                        .isSelected(true)
                        .isDefault(false)
                        .quantity(null)
                        .quantityCalculatorDto(null)
                        .price(BigDecimal.valueOf(1.99))
                        .traitCalculationResult(
                                new TraitCalculationResult(
                                        List.of(),
                                        BigDecimal.valueOf(0.25)
                                )
                        )
                        .build()
        );

        OptionCalculationResult result = optionCalculator.calculateTotalPrice(mockOptionCalculatorDtos);

        Assertions.assertEquals(new BigDecimal("2.24"), result.getOptionTotalPrice());
    }

    @Test
    public void givenNoneOptionWithoutCountType_whenCalculate_thenDoesNotThrow() {
        OptionCalculatorDto dto = OptionCalculatorDto
                .builder()
                .productOptionId(3L)
                .isSelected(true)
                .isDefault(false)
                .quantity(null)
                .quantityCalculatorDto(null)
                .price(BigDecimal.valueOf(3.00))
                .traitCalculationResult(
                        new TraitCalculationResult(
                                List.of(),
                                BigDecimal.ZERO
                        )
                )
                .build();

        assertDoesNotThrow(() -> optionCalculator.calculatePrice(dto));
    }

    @Test
    public void givenCountableOptionWithNullQuantity_whenCalculate_thenThrowsInvalidOptionRequestException() {
        OptionCalculatorDto dto = OptionCalculatorDto
                .builder()
                .productOptionId(4L)
                .countType(CountType.COUNTABLE)
                .isSelected(true)
                .isDefault(false)
                .quantity(null)
                .quantityCalculatorDto(null)
                .price(BigDecimal.valueOf(1.00))
                .traitCalculationResult(
                        new TraitCalculationResult(
                                List.of(),
                                BigDecimal.ZERO
                        )
                )
                .build();

        assertThrows(InvalidOptionRequestException.class, () -> optionCalculator.calculatePrice(dto));
    }
}
