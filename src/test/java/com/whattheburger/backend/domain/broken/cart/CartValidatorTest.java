package com.whattheburger.backend.domain.broken.cart;

import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.domain.cart.Cart;
import com.whattheburger.backend.domain.cart.CartList;
import com.whattheburger.backend.domain.cart.CartValidator;
import com.whattheburger.backend.service.dto.cart.ValidatedCartDto;
import com.whattheburger.backend.service.exception.ProductNotFoundException;
import com.whattheburger.backend.service.exception.ResourceNotFoundException;
import com.whattheburger.backend.utils.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartValidatorTest {

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

    CartValidator cartValidator = new CartValidator();

    @BeforeEach
    void setUp() {
        initMock();
        productMap.put(1L, mockProduct);
        customRuleMap.put(1L, mockCustomRule);
        productOptionMap.put(1L, mockProductOption);
        productOptionTraitMap.put(1L, mockProductOptionTrait);
    }
    @Test
    void givenCartAndMaps_whenValidate_thenReturnExpectedDto() {
        CartList mockCartList = MockCartFactory.createCartList();
        List<Cart> carts = mockCartList.getCarts();
        List<ValidatedCartDto> resultDto = cartValidator.validate(
                carts,
                productMap,
                customRuleMap,
                productOptionMap,
                productOptionTraitMap,
                quantityMap
        );

        Assertions.assertNotNull(resultDto);
        Assertions.assertEquals(resultDto.get(0).getValidatedProduct().getProduct().getId(), 1L);
        Assertions.assertEquals(resultDto.get(0).getValidatedCustomRules().get(0).getCustomRule().getId(), 1L);
        Assertions.assertEquals(resultDto.get(0).getValidatedCustomRules().get(0).getValidatedOptions().get(0).getProductOption().getId(), 1L);
        Assertions.assertEquals(resultDto.get(0).getValidatedCustomRules().get(0).getValidatedOptions().get(0).getIsSelected(), true);
        Assertions.assertEquals(resultDto.get(0).getValidatedCustomRules().get(0).getValidatedOptions().get(0).getValidatedTraits().get(0).getProductOptionTrait().getId(), 1L);
        Assertions.assertNull(resultDto.get(0).getValidatedCustomRules().get(0).getValidatedOptions().get(0).getValidatedQuantity());
    }

    @Test
    void givenNonExistingProductIdCartList_whenValidate_thenThrowProductNotFoundException() {
        CartList mockCartList = MockCartFactory.createNonExistingProductIdCartList();
        List<Cart> carts = mockCartList.getCarts();

        Assertions.assertThrows(ProductNotFoundException.class, () -> cartValidator.validate(
                carts,
                productMap,
                customRuleMap,
                productOptionMap,
                productOptionTraitMap,
                quantityMap
        ));
    }

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
