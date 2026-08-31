package com.whattheburger.backend.domain.cart;

import com.whattheburger.backend.controller.dto.cart.CustomRuleRequest;
import com.whattheburger.backend.controller.dto.cart.OptionRequest;
import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.domain.cart.strategy.BinaryStrategy;
import com.whattheburger.backend.domain.cart.strategy.TraitCalcStrategyResolver;
import com.whattheburger.backend.domain.enums.DeltaType;
import com.whattheburger.backend.service.dto.cart.calculator.CalculatedCartDto;
import com.whattheburger.backend.service.dto.cart.calculator.ProductCalculationDetail;
import com.whattheburger.backend.utils.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CartCalculatorTest {

    private static final BigDecimal BASE_PRODUCT_PRICE = BigDecimal.valueOf(5.99);
    private static final BigDecimal OVERRIDE_DELTA_PRICE = BigDecimal.valueOf(4.99);

    CartCalculator cartCalculator;

    Store baseStore;
    Product baseProduct;
    CustomRule baseCustomRule;
    Option baseOption;
    ProductOption baseProductOption;
    StoreProduct baseStoreProduct;

    Map<Long, StoreProduct> storeProductMap = new HashMap<>();
    Map<Long, CustomRule> customRuleMap = new HashMap<>();
    Map<Long, ProductOption> productOptionMap = new HashMap<>();

    @BeforeEach
    void setUp() {
        TraitCalcStrategyResolver strategyResolver = new TraitCalcStrategyResolver(List.of(new BinaryStrategy()));
        cartCalculator = new CartCalculator(
                new ProductCalculator(),
                new CustomRuleCalculator(),
                new OptionCalculator(),
                new TraitCalculator(strategyResolver)
        );
        initFixtures();
    }

    @Test
    void calculateProductPrice_whenOverrideDelta_appliesOverrideExtraPrice() {
        ProductCalculationDetail result = cartCalculator.calculateProductPrice(
                cartWithQuantity(1),
                storeProductMap,
                customRuleMap,
                productOptionMap,
                Map.of(),
                Map.of()
        );

        assertThat(result.getCalculatedExtraPrice()).isEqualByComparingTo(OVERRIDE_DELTA_PRICE);
        assertThat(result.getCalculatedTotalPrice())
                .isEqualByComparingTo(BASE_PRODUCT_PRICE.add(OVERRIDE_DELTA_PRICE));
    }

    @Test
    void calculateProductPrice_whenQuantityGreaterThanOne_multipliesUnitPriceByQuantity() {
        ProductCalculationDetail result = cartCalculator.calculateProductPrice(
                cartWithQuantity(2),
                storeProductMap,
                customRuleMap,
                productOptionMap,
                Map.of(),
                Map.of()
        );

        BigDecimal unitPrice = BASE_PRODUCT_PRICE.add(OVERRIDE_DELTA_PRICE);
        assertThat(result.getCalculatedExtraPrice()).isEqualByComparingTo(OVERRIDE_DELTA_PRICE);
        assertThat(result.getCalculatedTotalPrice())
                .isEqualByComparingTo(unitPrice.multiply(BigDecimal.valueOf(2)));
    }

    @Test
    void calculateTotalPrice_sumsProductLineTotalsWithoutReapplyingQuantity() {
        CalculatedCartDto result = cartCalculator.calculateTotalPrice(
                List.of(cartWithQuantity(2)),
                storeProductMap,
                customRuleMap,
                productOptionMap,
                Map.of(),
                Map.of()
        );

        BigDecimal expectedLineTotal = BASE_PRODUCT_PRICE.add(OVERRIDE_DELTA_PRICE)
                .multiply(BigDecimal.valueOf(2));
        assertThat(result.getCartCalculationResult().getCartTotalPrice())
                .isEqualByComparingTo(expectedLineTotal);
    }

    private Cart cartWithQuantity(int quantity) {
        return new Cart(
                1L,
                quantity,
                List.of(new CustomRuleRequest(
                        1L,
                        List.of(new OptionRequest(
                                1L,
                                1,
                                true,
                                List.of(),
                                null
                        ))
                ))
        );
    }

    private void initFixtures() {
        baseStore = MockStoreFactory.createStore();
        baseProduct = MockProductFactory.createMockProduct();
        baseCustomRule = MockCustomRuleFactory.createMockCustomRule();
        baseOption = MockOptionFactory.createMockOption();
        baseProductOption = MockOptionFactory.createMockProductOption(baseProduct, baseOption, baseCustomRule);

        StoreOptionDelta overrideDelta = new StoreOptionDelta(baseProductOption, null);
        overrideDelta.override(OVERRIDE_DELTA_PRICE, DeltaType.OVERRIDE);
        baseStoreProduct = MockStoreProductFactory.createStoreProduct(
                1L,
                null,
                true,
                baseStore,
                baseProduct,
                List.of(overrideDelta)
        );

        storeProductMap.put(1L, baseStoreProduct);
        customRuleMap.put(1L, baseCustomRule);
        productOptionMap.put(1L, baseProductOption);
    }
}
