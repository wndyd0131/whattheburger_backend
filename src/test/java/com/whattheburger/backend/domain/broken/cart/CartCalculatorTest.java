package com.whattheburger.backend.domain.broken.cart;

import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.domain.cart.*;
import com.whattheburger.backend.domain.enums.OptionTraitType;
import com.whattheburger.backend.service.dto.cart.calculator.*;
import com.whattheburger.backend.utils.*;
import com.whattheburger.backend.utils.broken.MockCalculatorDtoFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartCalculatorTest {
    @Mock
    ProductCalculator productCalculator;
    @Mock
    CustomRuleCalculator customRuleCalculator;
    @Mock
    OptionCalculator optionCalculator;
    @Mock
    TraitCalculator traitCalculator;
    @InjectMocks
    CartCalculator cartCalculator;

    Category mockCategory;
    Option mockOption;
    OptionTrait mockOptionTrait;
    Product mockProduct;
    CustomRule mockCustomRule;
    CategoryProduct mockCategoryProduct;
    ProductOption mockProductOption;
    ProductOptionTrait mockProductOptionTrait;
    Map<Long, Product> productMap = new HashMap<>();
    Map<Long, CustomRule> customRuleMap = new HashMap<>();
    Map<Long, ProductOption> productOptionMap = new HashMap<>();
    Map<Long, ProductOptionTrait> productOptionTraitMap = new HashMap<>();
    Map<Long, ProductOptionOptionQuantity> quantityMap = new HashMap<>();


    @BeforeEach
    void setUp() {
        initMock();
        productMap.put(1L, mockProduct);
        customRuleMap.put(1L, mockCustomRule);
        productOptionMap.put(1L, mockProductOption);
        productOptionTraitMap.put(1L, mockProductOptionTrait);
    }

    @Test
    void givenCartAndMaps_whenCalculate_thenReturnExpectedDto() {
        CartList mockCartList = MockCartFactory.createCartList();
        List<Cart> carts = mockCartList.getCarts();
        CalculatedCartDto calculatedCartDto = cartCalculator.calculateTotalPrice(
                carts,
                productMap,
                customRuleMap,
                productOptionMap,
                productOptionTraitMap,
                quantityMap
        );
    }

//    @Mock ProductCalculator productCalculator;
//
//    @InjectMocks
//    CartCalculator cartCalculator;
//
//    @Test
//    public void givenCalculatorDto_whenCalculate_thenReturnExpectedValue() {
//        CalculatorDto mockCalculatorDto = MockCalculatorDtoFactory.createMockCalculatorDto();
//        BigDecimal totalPrice = cartCalculator.calculate(mockCalculatorDto);
//
//        when(productCalculator.calculateTotalPrice(anyList())).thenReturn()
//
//        Assertions.assertEquals(totalPrice, 98.34D);
//    }

//
//    @Test
//    public void givenCalculatorDto_whenCalculate_thenReturnExpectedValue() {
//
//        CartList mockCartList = MockCartFactory.createCartList();
//        List<Cart> carts = mockCartList.getCarts();
//
//
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
//        List.of(
//                new ProductCalculation
//        )
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
//        CalculatedCartDto calculatedCartDto = cartCalculator.calculateTotalPrice(
//                carts,
//                productMap,
//                customRuleMap,
//                productOptionMap,
//                productOptionTraitMap,
//                quantityMap
//        );
//        Assertions.assertThat(totalPrice).isEqualTo(52.43);
//    }

    private void initMock() {
        mockCategory = MockCategoryFactory.createMockCategory();
        mockOption = MockOptionFactory.createMockOption();
        mockOptionTrait = MockOptionTraitFactory.createMockOptionTrait();
        mockProduct = MockProductFactory.createMockProduct();
        mockCustomRule = MockCustomRuleFactory.createMockCustomRule();
        mockCategoryProduct = MockProductFactory.createMockCategoryProduct(mockCategory, mockProduct);
        mockProductOption = MockOptionFactory.createMockProductOption(mockProduct, mockOption, mockCustomRule);
        mockProductOptionTrait = MockOptionTraitFactory.createMockProductOptionTrait(mockProductOption, mockOptionTrait);
    }
}
