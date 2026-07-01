package com.whattheburger.backend.domain.cart;

import com.whattheburger.backend.controller.dto.cart.CustomRuleRequest;
import com.whattheburger.backend.controller.dto.cart.OptionRequest;
import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.domain.enums.DeltaType;
import com.whattheburger.backend.service.dto.cart.ValidatedCartDto;
import com.whattheburger.backend.service.exception.HiddenOptionSelectedException;
import com.whattheburger.backend.utils.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CartValidatorTest {

    private static final Long STORE_ID = 1L;
    private static final Long STORE_PRODUCT_ID = 1L;
    private static final Long CUSTOM_RULE_ID = 1L;
    private static final Long PRODUCT_OPTION_ID = 1L;
    private static final BigDecimal OVERRIDE_DELTA_PRICE = BigDecimal.valueOf(4.99);

    CartValidator cartValidator;

    Store baseStore;
    Product baseProduct;
    CustomRule baseCustomRule;
    Option baseOption;
    ProductOption baseProductOption;

    Map<Long, StoreProduct> storeProductMap = new HashMap<>();
    Map<Long, CustomRule> customRuleMap = new HashMap<>();
    Map<Long, ProductOption> productOptionMap = new HashMap<>();

    @BeforeEach
    void setUp() {
        cartValidator = new CartValidator();
        initBaselineFixtures();
    }

    @Test
    void validate_whenHiddenDeltaAndOptionSelected_throwsHiddenOptionSelectedException() {
        givenStoreProduct(storeProductWithHiddenDelta());
        Cart cart = cartWithOptionSelected(true);

        assertThatThrownBy(() -> validate(cart))
                .isInstanceOf(HiddenOptionSelectedException.class);
    }

    @Test
    void validate_whenHiddenDeltaAndOptionNotSelected_passesValidation() {
        givenStoreProduct(storeProductWithHiddenDelta());
        Cart cart = cartWithOptionSelected(false);

        ValidatedCartDto result = validate(cart);

        assertThat(result.getValidatedProduct().getStoreProduct().getId()).isEqualTo(STORE_PRODUCT_ID);
        assertThat(result.getValidatedCustomRules()).hasSize(1);
    }

    @Test
    void validate_whenOverrideDeltaAndOptionSelected_passesValidation() {
        givenStoreProduct(storeProductWithOverrideDelta());
        Cart cart = cartWithOptionSelected(true);

        ValidatedCartDto result = validate(cart);

        assertThat(result.getValidatedCustomRules()).hasSize(1);
        assertThat(result.getValidatedCustomRules().get(0).getValidatedOptions()).hasSize(1);
    }

    @Test
    void validate_whenNoDeltaAndOptionSelected_passesValidation() {
        givenStoreProduct(storeProductWithNoDelta());
        Cart cart = cartWithOptionSelected(true);

        ValidatedCartDto result = validate(cart);

        assertThat(result.getValidatedCustomRules()).hasSize(1);
    }

    private void initBaselineFixtures() {
        baseStore = MockStoreFactory.createStore();
        baseProduct = MockProductFactory.createMockProduct();
        baseCustomRule = MockCustomRuleFactory.createMockCustomRule();
        baseOption = MockOptionFactory.createMockOption();
        baseProductOption = MockOptionFactory.createMockProductOption(baseProduct, baseOption, baseCustomRule);

        customRuleMap.put(CUSTOM_RULE_ID, baseCustomRule);
        productOptionMap.put(PRODUCT_OPTION_ID, baseProductOption);
    }

    private void givenStoreProduct(StoreProduct storeProduct) {
        storeProductMap.clear();
        storeProductMap.put(STORE_PRODUCT_ID, storeProduct);
    }

    private ValidatedCartDto validate(Cart cart) {
        return cartValidator.validate(
                STORE_ID,
                cart,
                storeProductMap,
                customRuleMap,
                productOptionMap,
                Map.of(),
                Map.of()
        );
    }

    private StoreProduct storeProductWithNoDelta() {
        return createStoreProduct(new ArrayList<>());
    }

    private StoreProduct storeProductWithHiddenDelta() {
        return createStoreProduct(List.of(hiddenDelta()));
    }

    private StoreProduct storeProductWithOverrideDelta() {
        return createStoreProduct(List.of(overrideDelta()));
    }

    private StoreProduct createStoreProduct(List<StoreOptionDelta> deltas) {
        return MockStoreProductFactory.createStoreProduct(
                STORE_PRODUCT_ID,
                null,
                true,
                baseStore,
                baseProduct,
                deltas
        );
    }

    private StoreOptionDelta hiddenDelta() {
        StoreOptionDelta delta = new StoreOptionDelta(baseProductOption, null);
        delta.override(null, DeltaType.HIDDEN);
        return delta;
    }

    private StoreOptionDelta overrideDelta() {
        StoreOptionDelta delta = new StoreOptionDelta(baseProductOption, null);
        delta.override(OVERRIDE_DELTA_PRICE, DeltaType.OVERRIDE);
        return delta;
    }

    private Cart cartWithOptionSelected(boolean isSelected) {
        return new Cart(
                STORE_PRODUCT_ID,
                1,
                List.of(new CustomRuleRequest(
                        CUSTOM_RULE_ID,
                        List.of(new OptionRequest(
                                PRODUCT_OPTION_ID,
                                1,
                                isSelected,
                                List.of(),
                                null
                        ))
                ))
        );
    }
}
