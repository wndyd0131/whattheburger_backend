package com.whattheburger.backend.service;

import com.whattheburger.backend.domain.Address;
import com.whattheburger.backend.domain.Coordinate;
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
import com.whattheburger.backend.repository.CustomRuleRepository;
import com.whattheburger.backend.repository.IngredientRepository;
import com.whattheburger.backend.repository.OptionRepository;
import com.whattheburger.backend.repository.ProductOptionRepository;
import com.whattheburger.backend.repository.ProductRepository;
import com.whattheburger.backend.repository.StoreInventoryRepository;
import com.whattheburger.backend.repository.StoreRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Dev/perf-only support for load testing {@link InventoryService#deductStock}.
 *
 * <p>Seeds two self-contained COUNTABLE fixtures (single-ingredient and multi-ingredient)
 * with very large initial stock, and builds synthetic PAID orders on demand so deduction
 * cost can be compared as the number of ingredients locked per transaction scales.
 *
 * <p>Only active when {@code perf.enabled=true}; intended for dev/staging, never prod.
 */
@Component
@ConditionalOnProperty(name = "perf.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class InventoryPerfSupport {

    public enum Scenario {
        SINGLE,
        MULTI
    }

    private static final int PRODUCT_QTY = 1;
    private static final int OPTION_QTY = 1;
    private static final int REQUIRED_QTY = 1;

    @Value("${perf.inventory.initial-stock:2000000000}")
    private int initialStock;

    @Value("${perf.inventory.multi-count:10}")
    private int multiCount;

    private final StoreRepository storeRepository;
    private final IngredientRepository ingredientRepository;
    private final OptionRepository optionRepository;
    private final CustomRuleRepository customRuleRepository;
    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;
    private final StoreInventoryRepository storeInventoryRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private volatile PerfFixture singleFixture;
    private volatile PerfFixture multiFixture;

    public record PerfFixture(
            Long storeId,
            Long customRuleId,
            List<Long> productOptionIds,
            List<Long> storeInventoryIds,
            int currentStock
    ) {}

    public record SetupResult(
            PerfFixture single,
            PerfFixture multi
    ) {}

    /**
     * Seeds both perf fixtures on first call and resets stock to {@code initialStock} on every
     * call so a fresh load-test run can start from a full stock level. Intended for
     * {@code /setup} only — not called from the deduct path.
     */
    @Transactional
    public synchronized SetupResult setupFixture() {
        singleFixture = ensureScenarioFixture(singleFixture, "Perf Single", 1);
        multiFixture = ensureScenarioFixture(multiFixture, "Perf Multi", multiCount);
        return new SetupResult(singleFixture, multiFixture);
    }

    /**
     * Builds an in-memory PAID order (no DB writes) referencing the seeded fixture for the
     * given scenario. {@link #setupFixture()} must have been called at least once beforehand.
     */
    public Order buildOrder(Scenario scenario) {
        PerfFixture current = resolveFixture(scenario);
        if (current == null) {
            throw new IllegalStateException("Perf fixture not initialized; call POST /api/v1/perf/inventory/setup first");
        }

        List<OrderProductOption> orderProductOptions = new ArrayList<>();
        for (Long productOptionId : current.productOptionIds()) {
            OrderProductOption orderProductOption = OrderProductOption.builder()
                    .productOptionId(productOptionId)
                    .countType(CountType.COUNTABLE)
                    .quantity(OPTION_QTY)
                    .build();
            orderProductOptions.add(orderProductOption);
        }

        OrderCustomRule orderCustomRule = OrderCustomRule.builder()
                .customRuleId(current.customRuleId())
                .name(scenario == Scenario.SINGLE ? "Perf Single" : "Perf Multi")
                .orderProductOptions(new ArrayList<>(orderProductOptions))
                .build();
        for (OrderProductOption orderProductOption : orderProductOptions) {
            orderProductOption.assignOrderCustomRule(orderCustomRule);
        }

        OrderProduct orderProduct = OrderProduct.builder()
                .storeProductId(1L)
                .quantity(PRODUCT_QTY)
                .name(scenario == Scenario.SINGLE ? "Perf Single Burger" : "Perf Multi Burger")
                .orderCustomRules(new ArrayList<>(List.of(orderCustomRule)))
                .build();
        orderCustomRule.assignOrderProduct(orderProduct);

        Store store = Store.builder().id(current.storeId()).build();

        return Order.builder()
                .store(store)
                .paymentStatus(PaymentStatus.PAID)
                .orderProducts(new ArrayList<>(List.of(orderProduct)))
                .build();
    }

    private PerfFixture ensureScenarioFixture(PerfFixture existing, String label, int ingredientCount) {
        if (existing == null) {
            PerfFixture created = createFixture(label, ingredientCount);
            log.info("Perf inventory fixture created ({} ingredients): {}", ingredientCount, created);
            return created;
        }

        resetStock(existing.storeInventoryIds());
        PerfFixture reset = new PerfFixture(
                existing.storeId(),
                existing.customRuleId(),
                existing.productOptionIds(),
                existing.storeInventoryIds(),
                initialStock
        );
        log.info("Perf inventory fixture stock reset to {} ({} ingredients)", initialStock, ingredientCount);
        return reset;
    }

    private void resetStock(List<Long> storeInventoryIds) {
        entityManager
                .createQuery("UPDATE StoreInventory si SET si.currentStock = :stock WHERE si.id IN :ids")
                .setParameter("stock", initialStock)
                .setParameter("ids", storeInventoryIds)
                .executeUpdate();
    }

    private PerfFixture resolveFixture(Scenario scenario) {
        return switch (scenario) {
            case SINGLE -> singleFixture;
            case MULTI -> multiFixture;
        };
    }

    private PerfFixture createFixture(String label, int ingredientCount) {
        Store store = storeRepository.save(
                Store.builder()
                        .branch(label + " Branch")
                        .houseNumber(1L)
                        .phoneNum("512-123-4567")
                        .website("www.whattheburger.com")
                        .address(new Address("Austin", "123 Main St", "TX", "78701"))
                        .coordinate(new Coordinate(30.0, -97.0))
                        .build()
        );

        Product product = productRepository.save(
                new Product(label + " Burger", BigDecimal.valueOf(5.99), "brief", 590D, ProductType.ONLY)
        );
        CustomRule customRule = customRuleRepository.save(
                new CustomRule(
                        label,
                        ingredientCount == 1 ? CustomRuleType.UNIQUE : CustomRuleType.LIMIT,
                        0,
                        1,
                        ingredientCount
                )
        );

        List<Long> productOptionIds = new ArrayList<>();
        List<Long> storeInventoryIds = new ArrayList<>();

        for (int i = 0; i < ingredientCount; i++) {
            String itemLabel = label + " Item " + (i + 1);
            Ingredient ingredient = ingredientRepository.save(
                    Ingredient.builder().name(itemLabel).unit(IngredientUnit.COUNT).build()
            );
            Option option = optionRepository.save(new Option(itemLabel, "/img/perf.jpg", 90D));
            entityManager.persist(new OptionIngredient(option, ingredient, REQUIRED_QTY));

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
                            i
                    )
            );
            StoreInventory storeInventory = storeInventoryRepository.save(
                    StoreInventory.builder()
                            .store(store)
                            .ingredient(ingredient)
                            .currentStock(initialStock)
                            .build()
            );

            productOptionIds.add(productOption.getId());
            storeInventoryIds.add(storeInventory.getId());
        }

        return new PerfFixture(
                store.getId(),
                customRule.getId(),
                List.copyOf(productOptionIds),
                List.copyOf(storeInventoryIds),
                initialStock
        );
    }

    public Map<String, Object> toResponseMap(SetupResult setupResult) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("single", setupResult.single());
        body.put("multi", setupResult.multi());
        return body;
    }
}
