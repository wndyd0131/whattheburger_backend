package com.whattheburger.backend.domain.inventory;

import com.whattheburger.backend.domain.Ingredient;
import com.whattheburger.backend.domain.Option;
import com.whattheburger.backend.domain.OptionIngredient;
import com.whattheburger.backend.domain.OptionQuantity;
import com.whattheburger.backend.domain.OptionQuantityIngredient;
import com.whattheburger.backend.domain.Product;
import com.whattheburger.backend.domain.ProductOption;
import com.whattheburger.backend.domain.ProductOptionOptionQuantity;
import com.whattheburger.backend.domain.enums.CountType;
import com.whattheburger.backend.service.exception.POOQuantityNotFoundException;
import com.whattheburger.backend.service.exception.ProductOptionNotFoundException;
import com.whattheburger.backend.service.exception.cart.InvalidOptionRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InventoryRequirementCalculatorTest {

    private static final long PRODUCT_OPTION_ID = 100L;
    private static final long PRODUCT_OPTION_ID_2 = 101L;
    private static final long INGREDIENT_ID = 300L;
    private static final long POOQ_ID = 400L;

    private InventoryRequirementCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new InventoryRequirementCalculator();
    }

    @Test
    @DisplayName("line의 countType이 countable일 때, 재고 계산 결과가 예상과 같아야 한다.")
    void givenCountableLine_whenAggregate_thenResultContainsIngredientWithExpectedDeduction() {
        ProductOption productOption = buildCountableProductOption(PRODUCT_OPTION_ID, INGREDIENT_ID, 3);

        Map<Long, Integer> result = calculator.aggregate(
                List.of(new StockRequirementLine(2, CountType.COUNTABLE, PRODUCT_OPTION_ID, 4, null)),
                Map.of(PRODUCT_OPTION_ID, productOption),
                Collections.emptyMap()
        );

        assertThat(result).containsEntry(INGREDIENT_ID, 2 * 4 * 3);
    }

    @Test
    @DisplayName("line의 countType이 uncountable일 때, 재고 계산 결과가 예상과 같아야 한다.")
    void givenUncountableLine_whenAggregate_thenResultContainsIngredientWithExpectedDeduction() {
        ProductOptionOptionQuantity pooq = buildUncountablePooq(POOQ_ID, INGREDIENT_ID, 5);

        Map<Long, Integer> result = calculator.aggregate(
                List.of(new StockRequirementLine(3, CountType.UNCOUNTABLE, PRODUCT_OPTION_ID, null, POOQ_ID)),
                Collections.emptyMap(),
                Map.of(POOQ_ID, pooq)
        );

        assertThat(result).containsEntry(INGREDIENT_ID, 3 * 5);
    }

    @Test
    @DisplayName("서로 같은 재고를 사용하는 line이 있을 때, 결과는 재고를 기준으로 합산되어야 한다.")
    void givenMultipleLinesSameIngredient_whenAggregate_thenMergesAmounts() {
        ProductOption productOption1 = buildCountableProductOption(PRODUCT_OPTION_ID, INGREDIENT_ID, 3);
        ProductOption productOption2 = buildCountableProductOption(PRODUCT_OPTION_ID_2, INGREDIENT_ID, 4);

        Map<Long, Integer> result = calculator.aggregate(
                List.of(
                        new StockRequirementLine(1, CountType.COUNTABLE, PRODUCT_OPTION_ID, 2, null),
                        new StockRequirementLine(1, CountType.COUNTABLE, PRODUCT_OPTION_ID_2, 1, null)
                ),
                Map.of(
                        PRODUCT_OPTION_ID, productOption1,
                        PRODUCT_OPTION_ID_2, productOption2
                ),
                Collections.emptyMap()
        );

        assertThat(result).containsEntry(INGREDIENT_ID, 1 * 2 * 3 + 1 * 1 * 4);
    }

    @Test
    @DisplayName("line의 countType이 none일 때, 재고 계산이 스킵되어야 한다.")
    void givenNoneCountTypeLine_whenAggregate_thenSkipsLine() {
        Map<Long, Integer> result = calculator.aggregate(
                List.of(new StockRequirementLine(1, CountType.NONE, PRODUCT_OPTION_ID, 1, null)),
                Collections.emptyMap(),
                Collections.emptyMap()
        );

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("line의 countType이 countable일 때, optionQuantity가 0보다 작거나 같은 경우 예외처리 해야 한다.")
    void givenCountableLineWithInvalidQuantity_whenAggregate_thenThrowsInvalidOptionRequestException() {
        assertThrows(InvalidOptionRequestException.class, () -> calculator.aggregate(
                List.of(new StockRequirementLine(1, CountType.COUNTABLE, PRODUCT_OPTION_ID, 0, null)),
                Collections.emptyMap(),
                Collections.emptyMap()
        ));
    }

    @Test
    @DisplayName("line의 countType이 uncountable일 때, productOptionOptionQuantityId가 null이면 예외처리 해야 한다.")
    void givenUncountableLineWithoutPooqId_whenAggregate_thenThrowsInvalidOptionRequestException() {
        assertThrows(InvalidOptionRequestException.class, () -> calculator.aggregate(
                List.of(new StockRequirementLine(1, CountType.UNCOUNTABLE, PRODUCT_OPTION_ID, null, null)),
                Collections.emptyMap(),
                Collections.emptyMap()
        ));
    }

    @Test
    @DisplayName("line의 countType이 countable일 때, productOption이 존재하지 않으면 예외처리 해야 한다.")
    void givenCountableLineAndMissingProductOption_whenAggregate_thenThrowsProductOptionNotFoundException() {
        assertThrows(ProductOptionNotFoundException.class, () -> calculator.aggregate(
                List.of(new StockRequirementLine(1, CountType.COUNTABLE, PRODUCT_OPTION_ID, 1, null)),
                Collections.emptyMap(),
                Collections.emptyMap()
        ));
    }

    @Test
    @DisplayName("line의 countType이 uncountable일 때, productOptionOptionQuantity가 존재하지 않으면 예외처리 해야 한다.")
    void givenUncountableLineAndMissingPooq_whenAggregate_thenThrowsPOOQuantityNotFoundException() {
        assertThrows(POOQuantityNotFoundException.class, () -> calculator.aggregate(
                List.of(new StockRequirementLine(1, CountType.UNCOUNTABLE, PRODUCT_OPTION_ID, null, POOQ_ID)),
                Collections.emptyMap(),
                Collections.emptyMap()
        ));
    }

    private ProductOption buildCountableProductOption(long productOptionId, long ingredientId, int requiredQuantity) {
        Ingredient ingredient = Ingredient.builder().id(ingredientId).build();
        OptionIngredient optionIngredient = OptionIngredient.builder()
                .id(1L)
                .ingredient(ingredient)
                .requiredQuantity(requiredQuantity)
                .build();
        Option option = Option.builder()
                .id(1L)
                .name("Cheese")
                .optionIngredients(new ArrayList<>(List.of(optionIngredient)))
                .build();
        return ProductOption.builder()
                .id(productOptionId)
                .countType(CountType.COUNTABLE)
                .option(option)
                .build();
    }

    private ProductOptionOptionQuantity buildUncountablePooq(long pooqId, long ingredientId, int requiredQuantity) {
        Ingredient ingredient = Ingredient.builder().id(ingredientId).build();
        OptionQuantityIngredient oqi = OptionQuantityIngredient.builder()
                .id(1L)
                .ingredient(ingredient)
                .requiredQuantity(requiredQuantity)
                .build();
        OptionQuantity optionQuantity = OptionQuantity.builder()
                .id(1L)
                .optionQuantityIngredients(new ArrayList<>(List.of(oqi)))
                .build();
        return ProductOptionOptionQuantity.builder()
                .id(pooqId)
                .optionQuantity(optionQuantity)
                .extraPrice(BigDecimal.ZERO)
                .build();
    }
}
