package com.whattheburger.backend.domain.broken.cart;

import com.whattheburger.backend.controller.dto.cart.CustomRuleRequest;
import com.whattheburger.backend.controller.dto.cart.OptionRequest;
import com.whattheburger.backend.controller.dto.cart.OptionTraitRequest;
import com.whattheburger.backend.controller.dto.cart.QuantityDetailRequest;
import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.domain.cart.*;
import com.whattheburger.backend.domain.enums.CountType;
import com.whattheburger.backend.domain.enums.CustomRuleType;
import com.whattheburger.backend.domain.enums.OptionTraitType;
import com.whattheburger.backend.domain.enums.ProductType;
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
import java.util.ArrayList;
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

    Product mockProduct1;
    Product mockProduct2;
    Category mockCategory;
    Option mockOption1;
    Option mockOption2;
    ProductOption mockProductOption1;
    ProductOption mockProductOption2;
    OptionTrait mockOptionTrait;
    Store mockStore;
    StoreProduct mockStoreProduct1;
    StoreProduct mockStoreProduct2;
    CustomRule mockCustomRule1;
    CustomRule mockCustomRule2;
    CategoryProduct mockCategoryProduct;
    ProductOptionTrait mockProductOptionTrait1;
    ProductOptionTrait mockProductOptionTrait2;

    Map<Long, StoreProduct> storeProductMap = new HashMap<>();
    Map<Long, CustomRule> customRuleMap = new HashMap<>();
    Map<Long, ProductOption> productOptionMap = new HashMap<>();
    Map<Long, ProductOptionTrait> productOptionTraitMap = new HashMap<>();
    Map<Long, ProductOptionOptionQuantity> quantityMap = new HashMap<>();


    @BeforeEach
    void setUp() {
        initMock();
        storeProductMap.put(1L, mockStoreProduct1);
        customRuleMap.put(1L, mockCustomRule1);
        customRuleMap.put(2L, mockCustomRule2);
        productOptionMap.put(1L, mockProductOption1);
        productOptionMap.put(2L, mockProductOption2);
        productOptionTraitMap.put(1L, mockProductOptionTrait1);
        productOptionTraitMap.put(2L, mockProductOptionTrait2);
    }

    @Test
    public void givenCalculatorDto_whenCalculate_thenReturnExpectedValue() {
        List<TraitCalculationDetail> traitCalculationDetails = List.of(
                TraitCalculationDetail
                        .builder()
                        .calculatedTraitPrice(BigDecimal.valueOf(0.99))
                        .productOptionTraitId(1L)
                        .build()
        );

        TraitCalculationResult mockTraitCalculationResult = TraitCalculationResult
                .builder()
                .traitTotalPrice(BigDecimal.valueOf(0.99))
                .traitCalculationDetails(traitCalculationDetails)
                .build();

        OptionCalculationResult mockOptionCalculationResult = OptionCalculationResult
                .builder()
                .optionTotalPrice(BigDecimal.valueOf(1.99))
                .optionCalculationDetails(
                        List.of(
                                OptionCalculationDetail
                                        .builder()
                                        .calculatedOptionPrice(BigDecimal.valueOf(1.99))
                                        .quantityCalculationDetail(null)
                                        .productOptionId(1L)
                                        .traitCalculationDetails(traitCalculationDetails)
                                        .build()
                        )
                )
                .build();
        CustomRuleCalculationResult mockCustomRuleCalculationResult = CustomRuleCalculationResult
                .builder()
                .customRuleTotalPrice(BigDecimal.valueOf(1.99))
                .customRuleCalculationDetails(null)
                .build();
        ProductCalculationDetail mockProductCalculationDetail1 = ProductCalculationDetail
                .builder()
                .calculatedTotalPrice(BigDecimal.valueOf(5.99))
                .calculatedExtraPrice(BigDecimal.ZERO)
                .customRuleCalculationDetails(null)
                .quantity(1)
                .storeProductId(1L)
                .build();
        ProductCalculationDetail mockProductCalculationDetail2 = ProductCalculationDetail
                .builder()
                .calculatedTotalPrice(BigDecimal.valueOf(10.00))
                .calculatedExtraPrice(BigDecimal.ZERO)
                .customRuleCalculationDetails(null)
                .quantity(2)
                .storeProductId(2L)
                .build();


        List<Cart> mockCarts = List.of(
                new Cart(
                        1L,
                        1,
                        List.of(new CustomRuleRequest(
                                1L,
                                List.of(
                                        new OptionRequest(
                                                1L,
                                                1,
                                                true,
                                                List.of(new OptionTraitRequest(
                                                        1L,
                                                        0
                                                )),
                                                null
                                        )
                                )
                        ))
                ),
                new Cart(
                        1L,
                        2,
                        List.of(new CustomRuleRequest(
                                1L,
                                List.of(
                                        new OptionRequest(
                                                1L,
                                                1,
                                                true,
                                                List.of(new OptionTraitRequest(
                                                        1L,
                                                        0
                                                )),
                                                null
                                        )
                                )
                        ))
                )
        );

        when(traitCalculator.calculateTotalPrice(any(List.class))).thenReturn(mockTraitCalculationResult);
        when(optionCalculator.calculateTotalPrice(any(List.class))).thenReturn(mockOptionCalculationResult);
        when(customRuleCalculator.calculateTotalPrice(any(List.class))).thenReturn(mockCustomRuleCalculationResult);
        when(productCalculator.calculatePrice(any(ProductCalculatorDto.class))).thenReturn(mockProductCalculationDetail1, mockProductCalculationDetail2);

        CalculatedCartDto calculatedCartDto = cartCalculator.calculateTotalPrice(
                mockCarts,
                storeProductMap,
                customRuleMap,
                productOptionMap,
                productOptionTraitMap,
                quantityMap
        );

        CartCalculationResult cartCalculationResult = calculatedCartDto.getCartCalculationResult();


        Assertions.assertEquals(BigDecimal.valueOf(25.99), cartCalculationResult.getCartTotalPrice());
    }

    @Test
    public void givenOverrideStoreProductPrice_whenCalculate_thenReturnExpectedValue() {
        List<TraitCalculationDetail> traitCalculationDetails = List.of(
                TraitCalculationDetail
                        .builder()
                        .calculatedTraitPrice(BigDecimal.valueOf(0.99))
                        .productOptionTraitId(1L)
                        .build()
        );

        TraitCalculationResult mockTraitCalculationResult = TraitCalculationResult
                .builder()
                .traitTotalPrice(BigDecimal.valueOf(0.99))
                .traitCalculationDetails(traitCalculationDetails)
                .build();

        OptionCalculationResult mockOptionCalculationResult = OptionCalculationResult
                .builder()
                .optionTotalPrice(BigDecimal.valueOf(1.99))
                .optionCalculationDetails(
                        List.of(
                                OptionCalculationDetail
                                        .builder()
                                        .calculatedOptionPrice(BigDecimal.valueOf(1.99))
                                        .quantityCalculationDetail(null)
                                        .productOptionId(1L)
                                        .traitCalculationDetails(traitCalculationDetails)
                                        .build()
                        )
                )
                .build();
        CustomRuleCalculationResult mockCustomRuleCalculationResult = CustomRuleCalculationResult
                .builder()
                .customRuleTotalPrice(BigDecimal.valueOf(1.99))
                .customRuleCalculationDetails(null)
                .build();
        ProductCalculationDetail mockProductCalculationDetail1 = ProductCalculationDetail
                .builder()
                .calculatedTotalPrice(BigDecimal.valueOf(5.99))
                .calculatedExtraPrice(BigDecimal.ZERO)
                .customRuleCalculationDetails(null)
                .quantity(1)
                .storeProductId(1L)
                .build();
        ProductCalculationDetail mockProductCalculationDetail2 = ProductCalculationDetail
                .builder()
                .calculatedTotalPrice(BigDecimal.valueOf(10.00))
                .calculatedExtraPrice(BigDecimal.ZERO)
                .customRuleCalculationDetails(null)
                .quantity(2)
                .storeProductId(2L)
                .build();


        List<Cart> mockCarts = List.of(
                new Cart(
                        1L,
                        1,
                        List.of(new CustomRuleRequest(
                                1L,
                                List.of(
                                        new OptionRequest(
                                                1L,
                                                1,
                                                true,
                                                List.of(new OptionTraitRequest(
                                                        1L,
                                                        0
                                                )),
                                                null
                                        )
                                )
                        ))
                ),
                new Cart(
                        1L,
                        2,
                        List.of(new CustomRuleRequest(
                                1L,
                                List.of(
                                        new OptionRequest(
                                                1L,
                                                1,
                                                true,
                                                List.of(new OptionTraitRequest(
                                                        1L,
                                                        0
                                                )),
                                                null
                                        )
                                )
                        ))
                )
        );

        when(traitCalculator.calculateTotalPrice(any(List.class))).thenReturn(mockTraitCalculationResult);
        when(optionCalculator.calculateTotalPrice(any(List.class))).thenReturn(mockOptionCalculationResult);
        when(customRuleCalculator.calculateTotalPrice(any(List.class))).thenReturn(mockCustomRuleCalculationResult);
        when(productCalculator.calculatePrice(any(ProductCalculatorDto.class))).thenReturn(mockProductCalculationDetail1, mockProductCalculationDetail2);

        CalculatedCartDto calculatedCartDto = cartCalculator.calculateTotalPrice(
                mockCarts,
                storeProductMap,
                customRuleMap,
                productOptionMap,
                productOptionTraitMap,
                quantityMap
        );

        CartCalculationResult cartCalculationResult = calculatedCartDto.getCartCalculationResult();


        Assertions.assertEquals(BigDecimal.valueOf(25.99), cartCalculationResult.getCartTotalPrice());
    }

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
        mockStore = MockStoreFactory.createStore(
                1L, "", "", null, 1L, new Coordinate(1D, 1D), "", null,
                null, null, 1L
        );
        mockProduct1 = MockProductFactory.createMockProduct(
                1L, "Whattheburger", BigDecimal.valueOf(5.99),
                "", "", 590D, ProductType.ONLY
        );
        mockProduct2 = MockProductFactory.createMockProduct(
                2L, "Whattheburger Jr.", BigDecimal.valueOf(3.99),
                "", "", 390D, ProductType.ONLY
        );
        mockCustomRule1 = MockCustomRuleFactory.createMockCustomRule(
                1L, "Bread", CustomRuleType.UNIQUE, 0, 1, 1
        );
        mockCustomRule2 = MockCustomRuleFactory.createMockCustomRule(
                1L, "Beef", CustomRuleType.UNIQUE, 1, 1, 1
        );
        mockOption1 = MockOptionFactory.createMockOption(
                1L, "Large Bun", "", 300D
        );
        mockOption2 = MockOptionFactory.createMockOption(
                2L, "Large Beef", "", 190D
        );
        mockOptionTrait = MockOptionTraitFactory.createMockOptionTrait(
                1L, "Toast Both Sides", "TBS", OptionTraitType.BINARY
        );
        mockStoreProduct1 = MockStoreProductFactory.createStoreProduct(
                1L, null, true, mockStore, mockProduct1
        );
        mockStoreProduct2 = MockStoreProductFactory.createStoreProduct(
                2L, null, true, mockStore, mockProduct2
        );
        mockProductOption1 = MockOptionFactory.createMockProductOption(
                1L, true, CountType.COUNTABLE, 1, 8,
                BigDecimal.ZERO, 0, mockProduct1, mockOption1, mockCustomRule1
        );
        mockProductOption2 = MockOptionFactory.createMockProductOption(
                2L, false, CountType.COUNTABLE, 1, 8,
                BigDecimal.valueOf(2.65), 0, mockProduct1, mockOption2, mockCustomRule2
        );
        mockProductOptionTrait1 = MockOptionTraitFactory.createMockProductOptionTrait(
                1L, 0, BigDecimal.ZERO, 10D, mockProductOption1, mockOptionTrait
        );
    }
}
