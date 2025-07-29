package com.whattheburger.backend.domain.cart;

import com.whattheburger.backend.service.dto.cart.calculator.ProductCalcDetail;
import com.whattheburger.backend.utils.MockCalculatorDtoFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

public class ProductCalculatorTest {

    ProductCalculator productCalculator = new ProductCalculator();

    @Test
    public void givenProductDetails_whenCalculate_thenReturnExpectedPrice() {
        List<ProductCalcDetail> mockProductCalcDetails = MockCalculatorDtoFactory.createMockProductCalcDetails();

        BigDecimal totalPrice = productCalculator.calculateTotalPrice(mockProductCalcDetails);

        Assertions.assertEquals(new BigDecimal("56.70"), totalPrice);
    }
}
