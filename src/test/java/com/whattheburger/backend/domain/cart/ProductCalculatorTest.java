package com.whattheburger.backend.domain.cart;

import com.whattheburger.backend.service.dto.cart.calculator.ProductCalculatorDto;
import com.whattheburger.backend.utils.MockCalculatorDtoFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

public class ProductCalculatorTest {

    ProductCalculator productCalculator = new ProductCalculator();

    @Test
    public void givenProductDetails_whenCalculate_thenReturnExpectedPrice() {
        List<ProductCalculatorDto> mockProductCalculatorDtos = MockCalculatorDtoFactory.createMockProductCalcDetails();

        BigDecimal totalPrice = productCalculator.calculateTotalPrice(mockProductCalculatorDtos);

        Assertions.assertEquals(new BigDecimal("56.70"), totalPrice);
    }
}
