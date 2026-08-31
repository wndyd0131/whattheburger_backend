package com.whattheburger.backend.integration;

import com.whattheburger.backend.domain.CustomRule;
import com.whattheburger.backend.domain.Ingredient;
import com.whattheburger.backend.domain.Option;
import com.whattheburger.backend.domain.OptionIngredient;
import com.whattheburger.backend.domain.OptionQuantity;
import com.whattheburger.backend.domain.OptionQuantityIngredient;
import com.whattheburger.backend.domain.Product;
import com.whattheburger.backend.domain.ProductOption;
import com.whattheburger.backend.domain.ProductOptionOptionQuantity;
import com.whattheburger.backend.domain.Quantity;
import com.whattheburger.backend.domain.Store;
import com.whattheburger.backend.domain.StoreInventory;
import com.whattheburger.backend.domain.enums.CountType;
import com.whattheburger.backend.domain.enums.CustomRuleType;
import com.whattheburger.backend.domain.enums.IngredientUnit;
import com.whattheburger.backend.domain.enums.PaymentStatus;
import com.whattheburger.backend.domain.enums.ProductType;
import com.whattheburger.backend.domain.enums.QuantityType;
import com.whattheburger.backend.domain.order.Order;
import com.whattheburger.backend.domain.order.OrderCustomRule;
import com.whattheburger.backend.domain.order.OrderProduct;
import com.whattheburger.backend.domain.order.OrderProductOption;
import com.whattheburger.backend.domain.order.QuantityDetail;
import com.whattheburger.backend.integration.support.BaseIntegrationTest;
import com.whattheburger.backend.integration.support.CatalogIntegrationFixture;
import com.whattheburger.backend.repository.CustomRuleRepository;
import com.whattheburger.backend.repository.IngredientRepository;
import com.whattheburger.backend.repository.OptionQuantityRepository;
import com.whattheburger.backend.repository.OptionRepository;
import com.whattheburger.backend.repository.ProductOptionOptionQuantityRepository;
import com.whattheburger.backend.repository.ProductOptionRepository;
import com.whattheburger.backend.repository.ProductRepository;
import com.whattheburger.backend.repository.QuantityRepository;
import com.whattheburger.backend.repository.StoreInventoryRepository;
import com.whattheburger.backend.service.InventoryService;
import com.whattheburger.backend.service.exception.cart.InsufficientOptionStockException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class InventoryServiceConcurrencyTest extends BaseIntegrationTest {

    private static final int THREAD_COUNT = 20;
    private static final int INITIAL_STOCK = 50;
    private static final int PRODUCT_QTY = 1;
    private static final int OPTION_QTY = 2;
    private static final int REQUIRED_QTY = 3;

    private static final int UNCOUNTABLE_INITIAL_STOCK = 50;
    private static final int UNCOUNTABLE_PRODUCT_QTY = 1;
    private static final int UNCOUNTABLE_REQUIRED_QTY = 3;

    @Autowired
    InventoryService inventoryService;

    @Autowired
    StoreInventoryRepository storeInventoryRepository;

    @Autowired
    CatalogIntegrationFixture catalog;

    @Autowired
    IngredientRepository ingredientRepository;

    @Autowired
    OptionRepository optionRepository;

    @Autowired
    CustomRuleRepository customRuleRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductOptionRepository productOptionRepository;

    @Autowired
    OptionQuantityRepository optionQuantityRepository;

    @Autowired
    ProductOptionOptionQuantityRepository productOptionOptionQuantityRepository;

    @Autowired
    QuantityRepository quantityRepository;

    @Autowired
    TransactionTemplate transactionTemplate;

    @PersistenceContext
    EntityManager entityManager;

    private record CountableScenario(
            Store store,
            ProductOption productOption,
            Ingredient ingredient,
            StoreInventory storeInventory,
            int deductionPerOrder
    ) {}

    private record UncountableScenario(
            Store store,
            ProductOption productOption,
            ProductOptionOptionQuantity productOptionOptionQuantity,
            Ingredient ingredient,
            StoreInventory storeInventory,
            int deductionPerOrder
    ) {}

    @Test
    @DisplayName("여러 사용자가 동시 결제 시 재고가 음수가 되지 않아야 한다.")
    void concurrentDeductStock_neverLeavesNegativeStock() throws InterruptedException {
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        CountableScenario scenario = saveCountableScenario(
                INITIAL_STOCK, PRODUCT_QTY, OPTION_QTY, REQUIRED_QTY
        );
        Order order = buildPaidOrder(
                scenario.store(),
                List.of(buildCountableOrderProduct(scenario, PRODUCT_QTY, OPTION_QTY))
        );

        runConcurrentDeductStock(order, successCount, failCount);

        List<StoreInventory> allIngredients = storeInventoryRepository.findAllByStoreId(scenario.store().getId());
        Assertions.assertThat(allIngredients).allMatch(inventory -> inventory.getCurrentStock() >= 0);
        Assertions.assertThat(successCount.get()).isEqualTo(8);
        Assertions.assertThat(failCount.get()).isEqualTo(12);
    }

    @Test
    @DisplayName("여러 사용자가 동시 결제 시 Countable 재고가 예상한 수만큼 남아야 한다.")
    void concurrentDeductStock_countableStockMatchesExpectedRemainder() throws InterruptedException {
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        CountableScenario scenario = saveCountableScenario(
                INITIAL_STOCK, PRODUCT_QTY, OPTION_QTY, REQUIRED_QTY
        );
        Order order = buildPaidOrder(
                scenario.store(),
                List.of(buildCountableOrderProduct(scenario, PRODUCT_QTY, OPTION_QTY))
        );

        int expectedSuccessCount = INITIAL_STOCK / scenario.deductionPerOrder();
        int expectedFailCount = THREAD_COUNT - expectedSuccessCount;
        int expectedRemainder = INITIAL_STOCK % scenario.deductionPerOrder();

        runConcurrentDeductStock(order, successCount, failCount);

        Assertions.assertThat(successCount.get()).isEqualTo(expectedSuccessCount);
        Assertions.assertThat(failCount.get()).isEqualTo(expectedFailCount);

        StoreInventory reloaded = storeInventoryRepository
                .findByStoreIdAndIngredientId(scenario.store().getId(), scenario.ingredient().getId())
                .orElseThrow();
        Assertions.assertThat(reloaded.getCurrentStock()).isEqualTo(expectedRemainder);
    }

    @Test
    @DisplayName("여러 사용자가 동시 결제 시 Uncountable 재고가 예상한 수만큼 남아야 한다.")
    void concurrentDeductStock_uncountableStockMatchesExpectedRemainder() throws InterruptedException {
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        UncountableScenario scenario = saveUncountableScenario(
                UNCOUNTABLE_INITIAL_STOCK, UNCOUNTABLE_PRODUCT_QTY, UNCOUNTABLE_REQUIRED_QTY
        );
        Order order = buildPaidOrder(
                scenario.store(),
                List.of(buildUncountableOrderProduct(scenario, UNCOUNTABLE_PRODUCT_QTY))
        );

        int expectedSuccessCount = UNCOUNTABLE_INITIAL_STOCK / scenario.deductionPerOrder();
        int expectedFailCount = THREAD_COUNT - expectedSuccessCount;
        int expectedRemainder = UNCOUNTABLE_INITIAL_STOCK % scenario.deductionPerOrder();

        runConcurrentDeductStock(order, successCount, failCount);

        Assertions.assertThat(successCount.get()).isEqualTo(expectedSuccessCount);
        Assertions.assertThat(failCount.get()).isEqualTo(expectedFailCount);

        StoreInventory reloaded = storeInventoryRepository
                .findByStoreIdAndIngredientId(scenario.store().getId(), scenario.ingredient().getId())
                .orElseThrow();
        Assertions.assertThat(reloaded.getCurrentStock()).isEqualTo(expectedRemainder);
    }

    private void runConcurrentDeductStock(
            Order order,
            AtomicInteger successCount,
            AtomicInteger failCount
    ) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch readyLatch = new CountDownLatch(THREAD_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();
                    inventoryService.deductStock(order);
                    successCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                } catch (InsufficientOptionStockException ignored) {
                    failCount.incrementAndGet();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        finishLatch.await();
        executor.shutdown();
    }

    private CountableScenario saveCountableScenario(
            int initialStock,
            int productQty,
            int optionQty,
            int requiredQuantity
    ) {
        return transactionTemplate.execute(status -> {
            Store store = catalog.saveStore("Concurrency Branch");
            Ingredient ingredient = ingredientRepository.save(
                    Ingredient.builder().name("Cheese").unit(IngredientUnit.COUNT).build()
            );
            Option option = optionRepository.save(new Option("Cheese", "/img/cheese.jpg", 90D));
            entityManager.persist(new OptionIngredient(option, ingredient, requiredQuantity));

            Product product = productRepository.save(
                    new Product("Burger", BigDecimal.valueOf(5.99), "brief", 590D, ProductType.ONLY)
            );
            CustomRule customRule = customRuleRepository.save(
                    new CustomRule("Cheese", CustomRuleType.UNIQUE, 0, 1, 1)
            );
            ProductOption productOption = productOptionRepository.save(
                    new ProductOption(
                            product,
                            option,
                            customRule,
                            false,
                            CountType.COUNTABLE,
                            1,
                            4,
                            BigDecimal.valueOf(1.00),
                            0
                    )
            );
            StoreInventory storeInventory = storeInventoryRepository.save(
                    StoreInventory.builder()
                            .store(store)
                            .ingredient(ingredient)
                            .currentStock(initialStock)
                            .build()
            );

            int deductionPerOrder = productQty * optionQty * requiredQuantity;
            return new CountableScenario(store, productOption, ingredient, storeInventory, deductionPerOrder);
        });
    }

    private UncountableScenario saveUncountableScenario(
            int initialStock,
            int productQty,
            int requiredQuantity
    ) {
        return transactionTemplate.execute(status -> {
            Store store = catalog.saveStore("Uncountable Concurrency Branch");
            Ingredient ingredient = ingredientRepository.save(
                    Ingredient.builder().name("Drink Syrup").unit(IngredientUnit.MILLILITER).build()
            );

            Quantity quantity = quantityRepository.save(
                    Quantity.builder()
                            .quantityType(QuantityType.Regular)
                            .labelCode("REG")
                            .build()
            );

            Option option = optionRepository.save(new Option("Coca-Cola", "/img/coke.jpg", 390D));
            OptionQuantity optionQuantity = optionQuantityRepository.save(
                    OptionQuantity.builder()
                            .option(option)
                            .quantity(quantity)
                            .extraCalories(0D)
                            .build()
            );
            entityManager.persist(new OptionQuantityIngredient(optionQuantity, ingredient, requiredQuantity));

            Product product = productRepository.save(
                    new Product("Drink", BigDecimal.valueOf(2.99), "brief", 390D, ProductType.ONLY)
            );
            CustomRule customRule = customRuleRepository.save(
                    new CustomRule("Drink Size", CustomRuleType.UNIQUE, 0, 1, 1)
            );
            ProductOption productOption = productOptionRepository.save(
                    new ProductOption(
                            product,
                            option,
                            customRule,
                            false,
                            CountType.UNCOUNTABLE,
                            1,
                            1,
                            BigDecimal.valueOf(0.00),
                            0
                    )
            );
            ProductOptionOptionQuantity productOptionOptionQuantity = productOptionOptionQuantityRepository.save(
                    new ProductOptionOptionQuantity(
                            productOption,
                            optionQuantity,
                            BigDecimal.ZERO,
                            true
                    )
            );
            StoreInventory storeInventory = storeInventoryRepository.save(
                    StoreInventory.builder()
                            .store(store)
                            .ingredient(ingredient)
                            .currentStock(initialStock)
                            .build()
            );

            int deductionPerOrder = productQty * requiredQuantity;
            return new UncountableScenario(
                    store,
                    productOption,
                    productOptionOptionQuantity,
                    ingredient,
                    storeInventory,
                    deductionPerOrder
            );
        });
    }

    private Order buildPaidOrder(Store store, List<OrderProduct> orderProducts) {
        return Order.builder()
                .store(store)
                .paymentStatus(PaymentStatus.PAID)
                .orderProducts(new ArrayList<>(orderProducts))
                .build();
    }

    private OrderProduct buildCountableOrderProduct(
            CountableScenario scenario,
            int productQty,
            int optionQty
    ) {
        OrderProductOption orderProductOption = OrderProductOption.builder()
                .productOptionId(scenario.productOption().getId())
                .countType(CountType.COUNTABLE)
                .quantity(optionQty)
                .build();

        OrderCustomRule orderCustomRule = OrderCustomRule.builder()
                .customRuleId(scenario.productOption().getCustomRule().getId())
                .name("Cheese")
                .orderProductOptions(new ArrayList<>(List.of(orderProductOption)))
                .build();
        orderProductOption.assignOrderCustomRule(orderCustomRule);

        OrderProduct orderProduct = OrderProduct.builder()
                .storeProductId(1L)
                .quantity(productQty)
                .name("Burger")
                .orderCustomRules(new ArrayList<>(List.of(orderCustomRule)))
                .build();
        orderCustomRule.assignOrderProduct(orderProduct);

        return orderProduct;
    }

    private OrderProduct buildUncountableOrderProduct(
            UncountableScenario scenario,
            int productQty
    ) {
        Quantity quantity = scenario.productOptionOptionQuantity().getOptionQuantity().getQuantity();
        OrderProductOption orderProductOption = OrderProductOption.builder()
                .productOptionId(scenario.productOption().getId())
                .countType(CountType.UNCOUNTABLE)
                .quantityDetail(new QuantityDetail(
                        scenario.productOptionOptionQuantity().getId(),
                        quantity.getQuantityType(),
                        BigDecimal.ZERO,
                        0.0
                ))
                .build();

        OrderCustomRule orderCustomRule = OrderCustomRule.builder()
                .customRuleId(scenario.productOption().getCustomRule().getId())
                .name("Drink Size")
                .orderProductOptions(new ArrayList<>(List.of(orderProductOption)))
                .build();
        orderProductOption.assignOrderCustomRule(orderCustomRule);

        OrderProduct orderProduct = OrderProduct.builder()
                .storeProductId(1L)
                .quantity(productQty)
                .name("Drink")
                .orderCustomRules(new ArrayList<>(List.of(orderCustomRule)))
                .build();
        orderCustomRule.assignOrderProduct(orderProduct);

        return orderProduct;
    }
}
