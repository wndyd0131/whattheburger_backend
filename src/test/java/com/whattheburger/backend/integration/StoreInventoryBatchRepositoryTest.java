package com.whattheburger.backend.integration;

import com.mysql.cj.conf.PropertyKey;
import com.mysql.cj.jdbc.JdbcConnection;
import com.whattheburger.backend.domain.CustomRule;
import com.whattheburger.backend.domain.Ingredient;
import com.whattheburger.backend.domain.Option;
import com.whattheburger.backend.domain.OptionIngredient;
import com.whattheburger.backend.domain.Product;
import com.whattheburger.backend.domain.ProductOption;
import com.whattheburger.backend.domain.Store;
import com.whattheburger.backend.domain.StoreInventory;
import com.whattheburger.backend.domain.enums.CountType;
import com.whattheburger.backend.domain.enums.CustomRuleType;
import com.whattheburger.backend.domain.enums.IngredientUnit;
import com.whattheburger.backend.domain.enums.PaymentStatus;
import com.whattheburger.backend.domain.enums.ProductType;
import com.whattheburger.backend.domain.order.Order;
import com.whattheburger.backend.domain.order.OrderCustomRule;
import com.whattheburger.backend.domain.order.OrderProduct;
import com.whattheburger.backend.domain.order.OrderProductOption;
import com.whattheburger.backend.integration.support.BaseIntegrationTest;
import com.whattheburger.backend.integration.support.CatalogIntegrationFixture;
import com.whattheburger.backend.repository.CustomRuleRepository;
import com.whattheburger.backend.repository.IngredientRepository;
import com.whattheburger.backend.repository.OptionRepository;
import com.whattheburger.backend.repository.ProductOptionRepository;
import com.whattheburger.backend.repository.ProductRepository;
import com.whattheburger.backend.repository.StoreInventoryBatchRepository;
import com.whattheburger.backend.repository.StoreInventoryRepository;
import com.whattheburger.backend.service.InventoryService;
import com.whattheburger.backend.service.exception.StoreInventoryNotFoundException;
import com.whattheburger.backend.service.exception.cart.InsufficientOptionStockException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class StoreInventoryBatchRepositoryTest extends BaseIntegrationTest {

    private static final int REQUIRED_QTY = 3;

    @Autowired
    StoreInventoryBatchRepository storeInventoryBatchRepository;
    @Autowired
    StoreInventoryRepository storeInventoryRepository;
    @Autowired
    InventoryService inventoryService;
    @Autowired
    CatalogIntegrationFixture catalog;
    @Autowired
    IngredientRepository ingredientRepository;
    @Autowired
    OptionRepository optionRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    CustomRuleRepository customRuleRepository;
    @Autowired
    ProductOptionRepository productOptionRepository;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    TransactionTemplate transactionTemplate;

    @PersistenceContext
    EntityManager entityManager;

    @Test
    void driverHasRewriteBatchedStatementsEnabled() {
        Boolean rewrite = jdbcTemplate.execute((ConnectionCallback<Boolean>) connection ->
                connection.unwrap(JdbcConnection.class)
                        .getPropertySet()
                        .getBooleanProperty(PropertyKey.rewriteBatchedStatements)
                        .getValue()
        );

        assertThat(rewrite).isTrue();
    }

    @Test
    void givenOneInsufficientRow_whenBatchDeduct_thenReturnsPerRowCounts() {
        Store store = saveStore("Batch Count Branch");
        Ingredient first = saveIngredientWithStock(store, "Cheese", 10);
        Ingredient second = saveIngredientWithStock(store, "Bacon", 2);
        Ingredient third = saveIngredientWithStock(store, "Onion", 10);
        List<Long> ingredientIds = List.of(first.getId(), second.getId(), third.getId());
        Map<Long, Integer> amounts = Map.of(first.getId(), 5, second.getId(), 5, third.getId(), 5);

        transactionTemplate.executeWithoutResult(status -> {
            int[] counts = storeInventoryBatchRepository.deductStockAtomicBatch(store.getId(), ingredientIds, amounts);

            assertThat(counts).containsExactly(1, 0, 1);
            assertThat(readStock(store, first)).isEqualTo(5);
            assertThat(readStock(store, second)).isEqualTo(2);
            assertThat(readStock(store, third)).isEqualTo(5);
            status.setRollbackOnly();
        });

        assertThat(readStock(store, first)).isEqualTo(10);
        assertThat(readStock(store, third)).isEqualTo(10);
    }

    @Test
    void givenMissingInventoryRow_whenBatchDeduct_thenReturnsZeroForThatRow() {
        Store store = saveStore("Batch Missing Branch");
        Ingredient first = saveIngredientWithStock(store, "Cheese", 10);
        Ingredient missing = saveIngredientWithoutStock("Pickle");
        Ingredient third = saveIngredientWithStock(store, "Onion", 10);
        List<Long> ingredientIds = List.of(first.getId(), missing.getId(), third.getId());
        Map<Long, Integer> amounts = Map.of(first.getId(), 1, missing.getId(), 1, third.getId(), 1);

        transactionTemplate.executeWithoutResult(status -> {
            int[] counts = storeInventoryBatchRepository.deductStockAtomicBatch(store.getId(), ingredientIds, amounts);

            assertThat(counts).containsExactly(1, 0, 1);
            status.setRollbackOnly();
        });
    }

    @Test
    void givenSecondIngredientInsufficient_whenDeductStock_thenThrowsAndRollsBackAllRows() {
        Store store = saveStore("Batch Insufficient Branch");
        Ingredient enough = saveIngredientWithStock(store, "Cheese", 100);
        Ingredient scarce = saveIngredientWithStock(store, "Bacon", 2);
        ProductOption productOption = saveCountableOption(List.of(enough, scarce));

        assertThatThrownBy(() -> inventoryService.deductStock(buildPaidOrder(store, productOption)))
                .isInstanceOf(InsufficientOptionStockException.class);

        assertThat(readStock(store, enough)).isEqualTo(100);
        assertThat(readStock(store, scarce)).isEqualTo(2);
    }

    @Test
    void givenMissingInventoryRow_whenDeductStock_thenThrowsNotFoundAndRollsBackAllRows() {
        Store store = saveStore("Batch NotFound Branch");
        Ingredient stocked = saveIngredientWithStock(store, "Cheese", 100);
        Ingredient missing = saveIngredientWithoutStock("Pickle");
        ProductOption productOption = saveCountableOption(List.of(stocked, missing));

        assertThatThrownBy(() -> inventoryService.deductStock(buildPaidOrder(store, productOption)))
                .isInstanceOf(StoreInventoryNotFoundException.class);

        assertThat(readStock(store, stocked)).isEqualTo(100);
    }

    private Store saveStore(String name) {
        return transactionTemplate.execute(status -> catalog.saveStore(name));
    }

    private Ingredient saveIngredientWithStock(Store store, String name, int stock) {
        return transactionTemplate.execute(status -> {
            Ingredient ingredient = ingredientRepository.save(
                    Ingredient.builder().name(name).unit(IngredientUnit.COUNT).build()
            );
            storeInventoryRepository.save(
                    StoreInventory.builder()
                            .store(entityManager.getReference(Store.class, store.getId()))
                            .ingredient(ingredient)
                            .currentStock(stock)
                            .build()
            );
            return ingredient;
        });
    }

    private Ingredient saveIngredientWithoutStock(String name) {
        return transactionTemplate.execute(status -> ingredientRepository.save(
                Ingredient.builder().name(name).unit(IngredientUnit.COUNT).build()
        ));
    }

    private ProductOption saveCountableOption(List<Ingredient> ingredients) {
        return transactionTemplate.execute(status -> {
            Option option = optionRepository.save(new Option("Combo", "/img/combo.jpg", 100D));
            for (Ingredient ingredient : ingredients) {
                entityManager.persist(new OptionIngredient(
                        option,
                        entityManager.getReference(Ingredient.class, ingredient.getId()),
                        REQUIRED_QTY
                ));
            }
            Product product = productRepository.save(
                    new Product("Burger", BigDecimal.valueOf(5.99), "brief", 590D, ProductType.ONLY)
            );
            CustomRule customRule = customRuleRepository.save(
                    new CustomRule("Combo", CustomRuleType.UNIQUE, 0, 1, 1)
            );
            return productOptionRepository.save(new ProductOption(
                    product,
                    option,
                    customRule,
                    false,
                    CountType.COUNTABLE,
                    1,
                    4,
                    BigDecimal.ONE,
                    0
            ));
        });
    }

    private Order buildPaidOrder(Store store, ProductOption productOption) {
        OrderProductOption orderProductOption = OrderProductOption.builder()
                .productOptionId(productOption.getId())
                .countType(CountType.COUNTABLE)
                .quantity(1)
                .build();
        OrderCustomRule orderCustomRule = OrderCustomRule.builder()
                .customRuleId(productOption.getCustomRule().getId())
                .name("Combo")
                .orderProductOptions(new ArrayList<>(List.of(orderProductOption)))
                .build();
        orderProductOption.assignOrderCustomRule(orderCustomRule);
        OrderProduct orderProduct = OrderProduct.builder()
                .storeProductId(1L)
                .quantity(1)
                .name("Burger")
                .orderCustomRules(new ArrayList<>(List.of(orderCustomRule)))
                .build();
        orderCustomRule.assignOrderProduct(orderProduct);

        return Order.builder()
                .store(store)
                .paymentStatus(PaymentStatus.PAID)
                .orderProducts(new ArrayList<>(List.of(orderProduct)))
                .build();
    }

    private int readStock(Store store, Ingredient ingredient) {
        Integer stock = jdbcTemplate.queryForObject(
                "SELECT current_stock FROM store_inventory WHERE store_id = ? AND ingredient_id = ?",
                Integer.class,
                store.getId(),
                ingredient.getId()
        );
        return stock;
    }
}
