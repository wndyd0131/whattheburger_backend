package com.whattheburger.backend.domain.cart;

import com.whattheburger.backend.service.dto.cart.calculator.CalculatorDto;
import com.whattheburger.backend.service.dto.cart.calculator.ProductDetail;
import com.whattheburger.backend.utils.MockCalculatorDtoFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductCalculatorTest {

    @Mock OptionCalculator optionCalculator;
    @InjectMocks ProductCalculator productCalculator;

    @Test
    public void givenProductDetails_whenCalculate_thenReturnExpectedPrice() {
        List<ProductDetail> mockProductDetails = MockCalculatorDtoFactory.createMockProductDetails();

        when(optionCalculator.calculateTotalPrice(anyList())).thenReturn(BigDecimal.ZERO);

        BigDecimal totalPrice = productCalculator.calculateTotalPrice(mockProductDetails);

        Assertions.assertEquals(BigDecimal.valueOf(36.45), totalPrice);
    }
}
