package com.whattheburger.backend.integration;

import com.whattheburger.backend.domain.CustomRule;
import com.whattheburger.backend.domain.Ingredient;
import com.whattheburger.backend.domain.Option;
import com.whattheburger.backend.domain.OptionIngredient;
import com.whattheburger.backend.domain.Product;
import com.whattheburger.backend.domain.ProductOption;
import com.whattheburger.backend.domain.Store;
import com.whattheburger.backend.domain.StoreInventory;
import com.whattheburger.backend.domain.StoreProduct;
import com.whattheburger.backend.domain.User;
import com.whattheburger.backend.domain.checkout.CheckoutAttempt;
import com.whattheburger.backend.domain.enums.CountType;
import com.whattheburger.backend.domain.enums.CustomRuleType;
import com.whattheburger.backend.domain.enums.IngredientUnit;
import com.whattheburger.backend.domain.enums.OrderStatus;
import com.whattheburger.backend.domain.enums.OrderType;
import com.whattheburger.backend.domain.enums.PaymentStatus;
import com.whattheburger.backend.domain.enums.ProductType;
import com.whattheburger.backend.domain.inventory.InventoryRequirementCalculator;
import com.whattheburger.backend.domain.order.*;
import com.whattheburger.backend.integration.support.BaseIntegrationTest;
import com.whattheburger.backend.integration.support.CartTestSupport;
import com.whattheburger.backend.integration.support.CatalogIntegrationFixture;
import com.whattheburger.backend.repository.*;
import com.whattheburger.backend.security.enums.Role;
import com.whattheburger.backend.service.InventoryService;
import com.whattheburger.backend.service.OrderService;
import com.whattheburger.backend.service.exception.cart.InsufficientOptionStockException;
import com.whattheburger.backend.service.exception.order.NonRetryableOrderProcessingException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class InventoryTransactionTest extends BaseIntegrationTest {

    private static final int PRODUCT_QTY = 1;
    private static final int OPTION_QTY = 2;
    private static final int REQUIRED_QTY = 3;

    @Autowired
    OrderService orderService;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    InventoryService inventoryService;
    @Autowired
    StoreInventoryRepository storeInventoryRepository;
    @Autowired
    ProductOptionRepository productOptionRepository;
    @Autowired
    ProductOptionOptionQuantityRepository productOptionOptionQuantityRepository;
    @Autowired
    InventoryRequirementCalculator inventoryRequirementCalculator;
    @Autowired
    CatalogIntegrationFixture catalog;
    @Autowired
    CartTestSupport cartTestSupport;
    @Autowired
    IngredientRepository ingredientRepository;
    @Autowired
    OptionRepository optionRepository;
    @Autowired
    CustomRuleRepository customRuleRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    StoreProductRepository storeProductRepository;
    @Autowired
    TransactionTemplate transactionTemplate;
    @Autowired
    CheckoutAttemptRepository checkoutAttemptRepository;

    @PersistenceContext
    EntityManager entityManager;

    private record CountableScenario(
            Store store,
            StoreProduct storeProduct,
            ProductOption productOption,
            Ingredient ingredient,
            StoreInventory storeInventory,
            int deductionPerOrder
    ) {}

    @Test
    void givenInsufficientStock_whenCompletePaidOrder_thenSuccessfullyRollback() throws Exception {
        CountableScenario scenario = saveCountableScenario(5);
        User user = cartTestSupport.saveUser(Role.USER);
        CheckoutAttempt checkoutAttempt = saveCheckoutAttempt(user, scenario);
        String checkoutSessionId = checkoutAttempt.getCheckoutSessionId();

        long prevOrderCount= orderRepository.count();

        Long storeInventoryId = scenario.storeInventory.getId();
        Integer prevStock = scenario.storeInventory.getCurrentStock();

        Assertions.assertThatThrownBy(() -> {
            orderService.completePaidOrder(checkoutAttempt, checkoutSessionId, null);
        }).isInstanceOf(NonRetryableOrderProcessingException.class)
                .hasCauseInstanceOf(InsufficientOptionStockException.class);

        // Order should not be saved
        assertThat(orderRepository.count()).isEqualTo(prevOrderCount);
        // Stock should stay same
        assertThat(storeInventoryRepository.findById(storeInventoryId).orElseThrow().getCurrentStock()).isEqualTo(prevStock);
    }

    @Test
    void givenExistingOrder_whenCompletePaidOrderFailsDueToFlushFail_thenSuccessfullyRollback() throws Exception {
        CountableScenario scenario = saveCountableScenario(50);
        User user = cartTestSupport.saveUser(Role.USER);
        CheckoutAttempt checkoutAttempt = saveCheckoutAttempt(user, scenario);
        String checkoutSessionId = checkoutAttempt.getCheckoutSessionId();

        orderRepository.save(Order.builder()
                .store(scenario.store())
                .user(user)
                .checkoutSessionId(checkoutSessionId)
                .orderType(OrderType.DELIVERY)
                .orderStatus(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PAID)
                .build());

        long prevOrderCount= orderRepository.count();

        Long storeInventoryId = scenario.storeInventory.getId();
        Integer prevStock = scenario.storeInventory.getCurrentStock();

        assertThatThrownBy(() -> {
            orderService.completePaidOrder(checkoutAttempt, checkoutSessionId, null);
        }).isInstanceOf(DataIntegrityViolationException.class);

        // Order should not be saved
        assertThat(orderRepository.count()).isEqualTo(prevOrderCount);
        // Stock should stay same
        assertThat(storeInventoryRepository.findById(storeInventoryId).orElseThrow().getCurrentStock()).isEqualTo(prevStock);
    }

    private CheckoutAttempt saveCheckoutAttempt(User user, CountableScenario scenario) {
        return checkoutAttemptRepository.save(CheckoutAttempt.builder()
                .storeId(scenario.store().getId())
                .userId(user.getId())
                .totalPrice(BigDecimal.valueOf(5.99))
                .orderType(OrderType.DELIVERY)
                .taxAmount(BigDecimal.ZERO)
                .paymentStatus(PaymentStatus.PENDING)
                .contactInfo(new ContactInfo("Test", "User", user.getEmail(), "5121234567"))
                .addressInfo(new AddressInfo("123 Main", "Apt 1", "78701", "Austin, TX"))
                .guestId(UUID.randomUUID())
                .checkoutSessionId("cs_test_" + UUID.randomUUID().toString().replace("-", ""))
                .orderRecord(List.of(buildOrderSessionProduct(scenario)))
                .build());
    }

    private OrderSessionProduct buildOrderSessionProduct(CountableScenario scenario) {
        OrderSessionOption orderSessionOption = OrderSessionOption.builder()
                .productOptionId(scenario.productOption().getId())
                .countType(CountType.COUNTABLE)
                .quantity(OPTION_QTY)
                .name(scenario.productOption().getOption().getName())
                .orderSessionOptionTraits(List.of())
                .build();

        OrderSessionCustomRule orderSessionCustomRule = OrderSessionCustomRule.builder()
                .customRuleId(scenario.productOption().getCustomRule().getId())
                .name("Cheese")
                .orderSessionOptions(List.of(orderSessionOption))
                .build();

        return OrderSessionProduct.builder()
                .storeProductId(scenario.storeProduct().getId())
                .quantity(PRODUCT_QTY)
                .name("Burger")
                .productType(ProductType.ONLY)
                .totalPrice(BigDecimal.valueOf(5.99))
                .orderSessionCustomRules(List.of(orderSessionCustomRule))
                .build();
    }

    private CountableScenario saveCountableScenario(int initialStock) {
        return transactionTemplate.execute(status -> {
            Store store = catalog.saveStore("Inventory Transaction Branch");
            Ingredient ingredient = ingredientRepository.save(
                    Ingredient.builder().name("Cheese").unit(IngredientUnit.COUNT).build()
            );
            Option option = optionRepository.save(new Option("Cheese", "/img/cheese.jpg", 90D));
            entityManager.persist(new OptionIngredient(option, ingredient, REQUIRED_QTY));

            Product product = productRepository.save(
                    new Product("Burger", BigDecimal.valueOf(5.99), "brief", 590D, ProductType.ONLY)
            );
            StoreProduct storeProduct = storeProductRepository.save(new StoreProduct(store, product));
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

            int deductionPerOrder = PRODUCT_QTY * OPTION_QTY * REQUIRED_QTY;
            return new CountableScenario(
                    store,
                    storeProduct,
                    productOption,
                    ingredient,
                    storeInventory,
                    deductionPerOrder
            );
        });
    }
}
