package com.whattheburger.backend.domain.cart;

import com.whattheburger.backend.domain.enums.OptionTraitType;
import com.whattheburger.backend.service.dto.cart.calculator.*;
import com.whattheburger.backend.utils.MockCalculatorDtoFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

public class CartCalculatorTest {

    CartCalculator cartCalculator = new CartCalculator();

    @Test
    public void givenCalculatorDto_whenCalculate_thenReturnExpectedValue() {
        CalculatorDto mockCalculatorDto = MockCalculatorDtoFactory.createMockCalculatorDto();
        BigDecimal totalPrice = cartCalculator.calculate(mockCalculatorDto);

        Assertions.assertEquals(totalPrice, 98.34D);
    }

//
//    @Test
//    public void givenCalculatorDto_whenCalculate_thenReturnExpectedValue() {
//        TraitDetail trait1 = MockCalculatorDtoFactory.createMockTraitDetail(5.45, 1, OptionTraitType.BINARY);
//        List<TraitDetail> traitDetails1 = List.of(
//                new TraitDetail(1D, 1, OptionTraitType.BINARY)
//        );
//        List<TraitDetail> traitDetails2 = List.of(
//                new TraitDetail(0.25, 0, OptionTraitType.BINARY)
//        );
//        List<TraitDetail> traitDetails3 = List.of(
//                new TraitDetail(0.25, 0, OptionTraitType.BINARY),
//                new TraitDetail(0.15, 1, OptionTraitType.BINARY)
//        );
//        List<TraitDetail> traitDetails4 = List.of();
//
//        QuantityDetail quantityDetail1 = new QuantityDetail(1.5);
//        QuantityDetail quantityDetail2 = new QuantityDetail(-0.35);
//
//        List<OptionDetail> optionDetails1 = List.of(
//                new OptionDetail(
//                        2D, 5, false, null, traitDetails1
//                ),
//                new OptionDetail(
//                        0.65, 2, true, quantityDetail1, traitDetails2
//                )
//        );
//        List<OptionDetail> optionDetails2 = List.of(
//                new OptionDetail(
//                        0D, 1, false, quantityDetail2, traitDetails3
//                ),
//                new OptionDetail(
//                        1.34, 4, false, null, traitDetails4
//                )
//        );
//        List<ProductDetail> productDetails = List.of(
//                new ProductDetail(
//                        5.99, 2, optionDetails1
//                ),
//                new ProductDetail(
//                        8.99, 1, optionDetails2
//                )
//        );
//        // product 1: (5.99 + 2 * 5 + 1 + 0.65 + 1.5) * 2 = 38.28
//        // product 2: (8.99 + 0 - 0.35 + 0.15 + 1.34 * 4) * 1 = 14.15
//
//        CalculatorDto calculatorDto = new CalculatorDto(productDetails);
//        Double totalPrice = cartCalculator.calculate(calculatorDto);
//        Assertions.assertThat(totalPrice).isEqualTo(52.43);
//    }
}
