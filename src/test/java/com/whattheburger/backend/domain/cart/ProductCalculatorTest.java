package com.whattheburger.backend.domain.cart;

import com.whattheburger.backend.service.dto.cart.calculator.CustomRuleCalculationResult;
import com.whattheburger.backend.service.dto.cart.calculator.ProductCalculationDetail;
import com.whattheburger.backend.service.dto.cart.calculator.ProductCalculationResult;
import com.whattheburger.backend.service.dto.cart.calculator.ProductCalculatorDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProductCalculatorTest {

    private final ProductCalculator productCalculator = new ProductCalculator();

    @Test
    void calculatePrice_whenQuantityIsOne_returnsUnitPrice() {
        ProductCalculatorDto dto = productDto(new BigDecimal("5.49"), new BigDecimal("2.99"), 1);

        ProductCalculationDetail result = productCalculator.calculatePrice(dto);

        assertThat(result.getCalculatedTotalPrice()).isEqualByComparingTo(new BigDecimal("8.48"));
        assertThat(result.getCalculatedExtraPrice()).isEqualByComparingTo(new BigDecimal("2.99"));
        assertThat(result.getQuantity()).isEqualTo(1);
    }

    @Test
    void calculatePrice_whenQuantityGreaterThanOne_multipliesUnitPriceByQuantity() {
        ProductCalculatorDto dto = productDto(new BigDecimal("5.49"), new BigDecimal("2.99"), 3);

        ProductCalculationDetail result = productCalculator.calculatePrice(dto);

        assertThat(result.getCalculatedTotalPrice()).isEqualByComparingTo(new BigDecimal("25.44"));
        assertThat(result.getCalculatedExtraPrice()).isEqualByComparingTo(new BigDecimal("2.99"));
        assertThat(result.getQuantity()).isEqualTo(3);
    }

    @Test
    void calculateTotalPrice_sumsLineTotals() {
        List<ProductCalculatorDto> dtos = List.of(
                productDto(new BigDecimal("5.49"), new BigDecimal("2.99"), 3),
                productDto(new BigDecimal("9.99"), new BigDecimal("5.64"), 2)
        );

        ProductCalculationResult result = productCalculator.calculateTotalPrice(dtos);

        assertThat(result.getProductTotalPrice()).isEqualByComparingTo(new BigDecimal("56.70"));
    }

    private ProductCalculatorDto productDto(BigDecimal basePrice, BigDecimal extraPrice, int quantity) {
        return ProductCalculatorDto.builder()
                .storeProductId(1L)
                .basePrice(basePrice)
                .quantity(quantity)
                .customRuleCalculationResult(
                        CustomRuleCalculationResult.builder()
                                .customRuleCalculationDetails(List.of())
                                .customRuleTotalPrice(extraPrice)
                                .build()
                )
                .build();
    }
}
