package com.whattheburger.backend.domain.cart;

import com.whattheburger.backend.controller.dto.cart.CustomRuleRequest;
import com.whattheburger.backend.controller.dto.cart.OptionRequest;
import com.whattheburger.backend.controller.dto.cart.QuantityDetailRequest;
import com.whattheburger.backend.domain.CustomRule;
import com.whattheburger.backend.domain.Ingredient;
import com.whattheburger.backend.domain.Option;
import com.whattheburger.backend.domain.OptionIngredient;
import com.whattheburger.backend.domain.OptionQuantity;
import com.whattheburger.backend.domain.OptionQuantityIngredient;
import com.whattheburger.backend.domain.Product;
import com.whattheburger.backend.domain.ProductOption;
import com.whattheburger.backend.domain.ProductOptionOptionQuantity;
import com.whattheburger.backend.domain.ProductOptionTrait;
import com.whattheburger.backend.domain.Store;
import com.whattheburger.backend.domain.StoreInventory;
import com.whattheburger.backend.domain.StoreProduct;
import com.whattheburger.backend.domain.enums.CountType;
import com.whattheburger.backend.domain.enums.ProductType;
import com.whattheburger.backend.domain.inventory.InventoryRequirementCalculator;
import com.whattheburger.backend.service.dto.cart.ValidatedCartDto;
import com.whattheburger.backend.service.exception.StoreInventoryNotFoundException;
import com.whattheburger.backend.service.exception.cart.CartItemLimitExceededException;
import com.whattheburger.backend.service.exception.cart.CartStoreProductNotFoundException;
import com.whattheburger.backend.service.exception.cart.InsufficientOptionStockException;
import com.whattheburger.backend.service.exception.cart.InvalidOptionRequestException;
import com.whattheburger.backend.service.exception.cart.StoreProductNotInStoreException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CartValidatorTest {

    private CartValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CartValidator(new InventoryRequirementCalculator());
    }

    @Nested
    class CanMergeItemCount {

        @Test
        public void givenMergeCountWithinLimit_whenCanMergeItemCount_thenReturnsTrue() {
            assertThat(validator.canMergeItemCount(10, 10)).isTrue();
        }

        @Test
        public void givenMergeCountExceedsLimit_whenCanMergeItemCount_thenReturnsFalse() {
            assertThat(validator.canMergeItemCount(15, 6)).isFalse();
        }
    }

    @Nested
    class ValidateCartList {

        @Test
        public void givenCartListExceedingMaxItems_whenValidate_thenThrowsCartItemLimitExceededException() {
            List<Cart> carts = new ArrayList<>();
            for (int i = 0; i < CartValidator.MAX_CART_ITEMS + 1; i++) {
                carts.add(new Cart(1L, 1, Collections.emptyList()));
            }
            CartList cartList = new CartList(1L, carts);

            assertThrows(CartItemLimitExceededException.class, () -> validator.validate(
                    cartList,
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap()
            ));
        }

        @Test
        public void givenCartListAtMaxItems_whenValidate_thenDoesNotThrowCartItemLimitExceededException() {
            List<Cart> carts = new ArrayList<>();
            for (int i = 0; i < CartValidator.MAX_CART_ITEMS; i++) {
                carts.add(new Cart(1L, 1, Collections.emptyList()));
            }
            CartList cartList = new CartList(1L, carts);

            Exception exception = assertThrows(Exception.class, () -> validator.validate(
                    cartList,
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap()
            ));
            assertThat(exception).isNotInstanceOf(CartItemLimitExceededException.class);
        }

        @Test
        public void givenCartListWithStoreProductFromAnotherStore_whenValidate_thenThrowsStoreProductNotInStoreException() {
            Long storeId = 1L;
            Long foreignStoreId = 99L;
            Long storeProductId = 42L;

            StoreProduct storeProduct = StoreProduct.builder()
                    .id(storeProductId)
                    .store(Store.builder().id(foreignStoreId).build())
                    .product(buildMockProduct())
                    .build();

            Cart cart = new Cart(storeProductId, 1, Collections.emptyList());
            CartList cartList = new CartList(storeId, new ArrayList<>(List.of(cart)));

            assertThrows(StoreProductNotInStoreException.class, () -> validator.validate(
                    cartList,
                    Map.of(storeProductId, storeProduct),
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap()
            ));
        }

        @Test
        public void givenCartList_whenValidate_thenReturnsValidatedCartDtoPerCart() {
            StockFixture fixture = stockFixture(CountType.COUNTABLE, true, /*optionQuantity*/ 2, /*currentStock*/ 100, /*requiredQuantity*/ 1);
            CartList cartList = new CartList(fixture.storeId, new ArrayList<>(List.of(fixture.cart, fixture.cart)));

            List<ValidatedCartDto> dtos = validator.validate(
                    cartList,
                    fixture.storeProductMap,
                    fixture.customRuleMap,
                    fixture.productOptionMap,
                    fixture.productOptionTraitMap,
                    fixture.quantityMap,
                    fixture.storeInventoryMap
            );

            assertThat(dtos).hasSize(2);
        }

        @Test
        public void givenCartListWithCombinedIngredientUsage_whenValidate_thenAggregatesStockAcrossItems() {
            StockFixture fixture = stockFixture(
                    CountType.COUNTABLE,
                    true,
                    1,
                    2,
                    10,
                    3
            );
            CartList cartList = new CartList(fixture.storeId, new ArrayList<>(List.of(fixture.cart, fixture.cart)));

            assertThrows(InsufficientOptionStockException.class, () -> validator.validate(
                    cartList,
                    fixture.storeProductMap,
                    fixture.customRuleMap,
                    fixture.productOptionMap,
                    fixture.productOptionTraitMap,
                    fixture.quantityMap,
                    fixture.storeInventoryMap
            ));
        }
    }

    @Nested
    class ValidateSingleCartStore {

        @Test
        public void givenStoreProductNotInMap_thenThrowsCartStoreProductNotFoundException() {
            Long storeId = 1L;
            Long storeProductId = 42L;
            Cart cart = new Cart(storeProductId, 1, Collections.emptyList());

            assertThrows(CartStoreProductNotFoundException.class, () -> validator.validate(
                    storeId,
                    cart,
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap()
            ));
        }

        @Test
        public void givenStoreProductFromAnotherStore_thenThrowsStoreProductNotInStoreException() {
            Long storeId = 1L;
            Long foreignStoreId = 99L;
            Long storeProductId = 42L;

            StoreProduct storeProduct = StoreProduct.builder()
                    .id(storeProductId)
                    .store(Store.builder().id(foreignStoreId).build())
                    .product(buildMockProduct())
                    .build();

            Cart cart = new Cart(storeProductId, 1, Collections.emptyList());

            assertThrows(StoreProductNotInStoreException.class, () -> validator.validate(
                    storeId,
                    cart,
                    Map.of(storeProductId, storeProduct),
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap()
            ));
        }
    }

    @Nested
    class ValidateSingleCartStock {

        @Test
        public void givenCountableOptionExceedingStock_thenThrowsInsufficientOptionStockException() {
            StockFixture fixture = stockFixture(CountType.COUNTABLE, /*isSelected*/ true, /*optionQuantity*/ 5, /*currentStock*/ 5, /*requiredQuantity*/ 2);

            assertThrows(InsufficientOptionStockException.class, () -> validate(fixture));
        }

        @Test
        public void givenCountableOptionWithEnoughStock_thenReturnsValidatedCartDto() {
            StockFixture fixture = stockFixture(CountType.COUNTABLE, true, /*optionQuantity*/ 3, /*currentStock*/ 50, /*requiredQuantity*/ 2);

            ValidatedCartDto dto = assertDoesNotThrow(() -> validate(fixture));

            assertThat(dto).isNotNull();
            assertThat(dto.getValidatedProduct().getStoreProduct().getId()).isEqualTo(fixture.storeProductId);
            assertThat(dto.getValidatedCustomRules()).hasSize(1);
        }

        @Test
        public void givenCountableOptionWithMissingInventory_thenThrowsStoreInventoryNotFoundException() {
            StockFixture fixture = stockFixture(CountType.COUNTABLE, true, /*optionQuantity*/ 1, /*currentStock*/ 100, /*requiredQuantity*/ 1);
            fixture.storeInventoryMap = Collections.emptyMap();

            assertThrows(StoreInventoryNotFoundException.class, () -> validate(fixture));
        }

        @Test
        public void givenUncountableOptionExceedingStock_thenThrowsInsufficientOptionStockException() {
            StockFixture fixture = uncountableStockFixture(/*currentStock*/ 1, /*requiredQuantity*/ 4);

            assertThrows(InsufficientOptionStockException.class, () -> validate(fixture));
        }

        @Test
        public void givenSelectedCountableOptionWithZeroQuantity_thenThrowsInvalidOptionRequestException() {
            StockFixture fixture = stockFixture(CountType.COUNTABLE, true, /*optionQuantity*/ 0, /*currentStock*/ 100, /*requiredQuantity*/ 1);

            assertThrows(InvalidOptionRequestException.class, () -> validate(fixture));
        }

        @Test
        public void givenSelectedUncountableOptionWithoutQuantityDetail_thenThrowsInvalidOptionRequestException() {
            StockFixture fixture = stockFixture(CountType.UNCOUNTABLE, true, /*optionQuantity (ignored)*/ 1, /*currentStock*/ 100, /*requiredQuantity*/ 1);

            assertThrows(InvalidOptionRequestException.class, () -> validate(fixture));
        }

        @Test
        public void givenNoneCountTypeOption_thenSkipsStockCheck() {
            StockFixture fixture = stockFixture(CountType.NONE, true, /*optionQuantity*/ 9999, /*currentStock*/ 0, /*requiredQuantity*/ 99);
            fixture.storeInventoryMap = Collections.emptyMap();

            assertDoesNotThrow(() -> validate(fixture));
        }

        @Test
        public void givenUnselectedOption_thenSkipsStockCheck() {
            StockFixture fixture = stockFixture(CountType.COUNTABLE, /*isSelected*/ false, 9999, /*currentStock*/ 0, /*requiredQuantity*/ 99);
            fixture.storeInventoryMap = Collections.emptyMap();

            assertDoesNotThrow(() -> validate(fixture));
        }

        @Test
        public void givenProductQuantityGreaterThanOne_whenValidate_thenUsesProductQuantityInStockCheck() {
            StockFixture fixture = stockFixture(
                    CountType.COUNTABLE,
                    true,
                    3,
                    2,
                    10,
                    2
            );

            assertThrows(InsufficientOptionStockException.class, () -> validate(fixture));
        }
    }

    private ValidatedCartDto validate(StockFixture fixture) {
        return validator.validate(
                fixture.storeId,
                fixture.cart,
                fixture.storeProductMap,
                fixture.customRuleMap,
                fixture.productOptionMap,
                fixture.productOptionTraitMap,
                fixture.quantityMap,
                fixture.storeInventoryMap
        );
    }

    private static class StockFixture {
        Long storeId = 1L;
        Long storeProductId = 42L;
        Long productOptionId = 100L;
        Long customRuleId = 200L;
        Long ingredientId = 300L;
        Long pooqId = 400L;

        Cart cart;
        Map<Long, StoreProduct> storeProductMap;
        Map<Long, CustomRule> customRuleMap;
        Map<Long, ProductOption> productOptionMap;
        Map<Long, ProductOptionTrait> productOptionTraitMap = Collections.emptyMap();
        Map<Long, ProductOptionOptionQuantity> quantityMap = Collections.emptyMap();
        Map<Long, StoreInventory> storeInventoryMap;
    }

    private StockFixture stockFixture(CountType countType, boolean isSelected, int optionQuantity, int currentStock, int requiredQuantity) {
        return stockFixture(countType, isSelected, 1, optionQuantity, currentStock, requiredQuantity);
    }

    private StockFixture stockFixture(
            CountType countType,
            boolean isSelected,
            int productQuantity,
            int optionQuantity,
            int currentStock,
            int requiredQuantity
    ) {
        StockFixture f = new StockFixture();

        Product product = buildMockProduct();
        Store store = Store.builder().id(f.storeId).build();
        StoreProduct storeProduct = StoreProduct.builder()
                .id(f.storeProductId)
                .store(store)
                .product(product)
                .build();

        Ingredient ingredient = Ingredient.builder().id(f.ingredientId).build();
        OptionIngredient optionIngredient = OptionIngredient.builder()
                .id(1L)
                .ingredient(ingredient)
                .requiredQuantity(requiredQuantity)
                .build();
        Option option = Option.builder()
                .id(1L)
                .name("Cheese")
                .imageSource("")
                .calories(10D)
                .productOptions(new ArrayList<>())
                .optionIngredients(new ArrayList<>(List.of(optionIngredient)))
                .build();

        CustomRule customRule = CustomRule.builder()
                .id(f.customRuleId)
                .name("Cheese")
                .productOptions(new ArrayList<>())
                .build();

        ProductOption productOption = ProductOption.builder()
                .id(f.productOptionId)
                .countType(countType)
                .option(option)
                .product(product)
                .extraPrice(BigDecimal.valueOf(2.99))
                .customRule(customRule)
                .productOptionTraits(new ArrayList<>())
                .productOptionOptionQuantities(new ArrayList<>())
                .build();

        StoreInventory storeInventory = StoreInventory.builder()
                .id(1L)
                .ingredient(ingredient)
                .store(store)
                .currentStock(currentStock)
                .build();

        OptionRequest optionRequest = OptionRequest.builder()
                .productOptionId(f.productOptionId)
                .optionQuantity(optionQuantity)
                .isSelected(isSelected)
                .optionTraitRequests(Collections.emptyList())
                .build();
        CustomRuleRequest customRuleRequest = CustomRuleRequest.builder()
                .customRuleId(f.customRuleId)
                .optionRequests(List.of(optionRequest))
                .build();

        f.cart = new Cart(f.storeProductId, productQuantity, List.of(customRuleRequest));
        f.storeProductMap = Map.of(f.storeProductId, storeProduct);
        f.customRuleMap = Map.of(f.customRuleId, customRule);
        f.productOptionMap = Map.of(f.productOptionId, productOption);
        f.storeInventoryMap = Map.of(f.ingredientId, storeInventory);

        return f;
    }

    private StockFixture uncountableStockFixture(int currentStock, int requiredQuantity) {
        StockFixture f = new StockFixture();

        Product product = buildMockProduct();
        Store store = Store.builder().id(f.storeId).build();
        StoreProduct storeProduct = StoreProduct.builder()
                .id(f.storeProductId)
                .store(store)
                .product(product)
                .build();

        Ingredient ingredient = Ingredient.builder().id(f.ingredientId).build();
        OptionQuantityIngredient optionQuantityIngredient = OptionQuantityIngredient.builder()
                .id(1L)
                .ingredient(ingredient)
                .requiredQuantity(requiredQuantity)
                .build();

        OptionQuantity optionQuantity = OptionQuantity.builder()
                .optionQuantityIngredients(new ArrayList<>(List.of(optionQuantityIngredient)))
                .build();

        Option option = Option.builder()
                .id(1L)
                .name("Drink")
                .imageSource("")
                .calories(0D)
                .productOptions(new ArrayList<>())
                .optionIngredients(new ArrayList<>())
                .build();

        CustomRule customRule = CustomRule.builder()
                .id(f.customRuleId)
                .name("Drink Size")
                .productOptions(new ArrayList<>())
                .build();

        ProductOption productOption = ProductOption.builder()
                .id(f.productOptionId)
                .countType(CountType.UNCOUNTABLE)
                .option(option)
                .product(product)
                .customRule(customRule)
                .extraPrice(BigDecimal.valueOf(2.99))
                .productOptionTraits(new ArrayList<>())
                .productOptionOptionQuantities(new ArrayList<>())
                .build();

        ProductOptionOptionQuantity productOptionOptionQuantity = ProductOptionOptionQuantity.builder()
                .id(f.pooqId)
                .productOption(productOption)
                .optionQuantity(optionQuantity)
                .extraPrice(BigDecimal.ZERO)
                .isDefault(true)
                .build();
        productOption.getProductOptionOptionQuantities().add(productOptionOptionQuantity);

        StoreInventory storeInventory = StoreInventory.builder()
                .id(1L)
                .ingredient(ingredient)
                .store(store)
                .currentStock(currentStock)
                .build();

        OptionRequest optionRequest = OptionRequest.builder()
                .productOptionId(f.productOptionId)
                .optionQuantity(1)
                .isSelected(true)
                .optionTraitRequests(Collections.emptyList())
                .quantityDetailRequest(QuantityDetailRequest.builder().id(f.pooqId).build())
                .build();
        CustomRuleRequest customRuleRequest = CustomRuleRequest.builder()
                .customRuleId(f.customRuleId)
                .optionRequests(List.of(optionRequest))
                .build();

        f.cart = new Cart(f.storeProductId, 1, List.of(customRuleRequest));
        f.storeProductMap = Map.of(f.storeProductId, storeProduct);
        f.customRuleMap = Map.of(f.customRuleId, customRule);
        f.productOptionMap = Map.of(f.productOptionId, productOption);
        f.quantityMap = Map.of(f.pooqId, productOptionOptionQuantity);
        f.storeInventoryMap = Map.of(f.ingredientId, storeInventory);

        return f;
    }

    private static Product buildMockProduct() {
        return Product.builder()
                .id(1L)
                .name("Whattheburger")
                .price(BigDecimal.valueOf(5.99))
                .briefInfo("")
                .imageSource("")
                .calories(590D)
                .productType(ProductType.ONLY)
                .productOptions(new ArrayList<>())
                .categoryProducts(new ArrayList<>())
                .build();
    }
}
