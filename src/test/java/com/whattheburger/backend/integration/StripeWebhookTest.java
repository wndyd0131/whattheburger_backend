package com.whattheburger.backend.integration;

import com.stripe.model.PaymentIntent;
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
import com.whattheburger.backend.domain.order.AddressInfo;
import com.whattheburger.backend.domain.order.ContactInfo;
import com.whattheburger.backend.domain.order.Order;
import com.whattheburger.backend.domain.order.OrderSession;
import com.whattheburger.backend.domain.order.OrderSessionCustomRule;
import com.whattheburger.backend.domain.order.OrderSessionOption;
import com.whattheburger.backend.domain.order.OrderSessionProduct;
import com.whattheburger.backend.domain.order.OrderSessionStorage;
import com.whattheburger.backend.integration.support.BaseIntegrationTest;
import com.whattheburger.backend.integration.support.CartTestSupport;
import com.whattheburger.backend.integration.support.CatalogIntegrationFixture;
import com.whattheburger.backend.integration.support.StripeWebhookTestSupport;
import com.whattheburger.backend.repository.CheckoutAttemptRepository;
import com.whattheburger.backend.repository.CustomRuleRepository;
import com.whattheburger.backend.repository.IngredientRepository;
import com.whattheburger.backend.repository.OptionRepository;
import com.whattheburger.backend.repository.OrderRepository;
import com.whattheburger.backend.repository.ProductOptionRepository;
import com.whattheburger.backend.repository.ProductRepository;
import com.whattheburger.backend.repository.StoreInventoryRepository;
import com.whattheburger.backend.repository.StoreProductRepository;
import com.whattheburger.backend.security.enums.Role;
import com.whattheburger.backend.service.OrderService;
import com.whattheburger.backend.service.S3Service;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class StripeWebhookTest extends BaseIntegrationTest {

    private static final int PRODUCT_QTY = 1;
    private static final int OPTION_QTY = 2;
    private static final int REQUIRED_QTY = 3;

    @Autowired
    MockMvc mockMvc;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    CheckoutAttemptRepository checkoutAttemptRepository;
    @Autowired
    CatalogIntegrationFixture catalog;
    @Autowired
    CartTestSupport cartTestSupport;
    @Autowired
    StripeWebhookTestSupport stripeWebhookTestSupport;
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
    StoreProductRepository storeProductRepository;
    @Autowired
    StoreInventoryRepository storeInventoryRepository;
    @Autowired
    OrderSessionStorage orderSessionStorage;
    @Autowired
    TransactionTemplate transactionTemplate;

    @PersistenceContext
    EntityManager entityManager;

    @MockBean
    S3Service s3Service;

    @SpyBean
    OrderService orderService;

    @Value("${stripe.webhook.secret}")
    String webhookSecret;

    @AfterEach
    void resetSpies() {
        Mockito.reset(orderService);
    }

    private record CountableScenario(
            Store store,
            StoreProduct storeProduct,
            ProductOption productOption,
            Ingredient ingredient,
            StoreInventory storeInventory
    ) {}

    @Test
    void paymentSuccess_whenOrderSessionIsMissing_successfullyCreatesOrder() throws Exception {
        CountableScenario scenario = saveCountableScenario(50);
        User user = cartTestSupport.saveUser(Role.USER);
        CheckoutAttempt checkoutAttempt = saveCheckoutAttempt(user, scenario, null);
        String checkoutSessionId = checkoutAttempt.getCheckoutSessionId();
        String paymentIntentId = "pi_test_" + UUID.randomUUID().toString().replace("-", "");

        long prevOrderCount = orderRepository.count();
        String payload = stripeWebhookTestSupport.buildCheckoutSessionCompletedPayload(
                checkoutSessionId,
                paymentIntentId,
                checkoutAttempt.getCheckoutAttemptId(),
                checkoutAttempt.getOrderSessionId()
        );
        String signature = stripeWebhookTestSupport.signPayload(payload, webhookSecret);

        try (MockedStatic<PaymentIntent> mocked = stripeWebhookTestSupport.mockPaymentIntentRetrieve(paymentIntentId)) {
            stripeWebhookTestSupport.postWebhook(mockMvc, payload, signature)
                    .andExpect(status().isOk());
        }

        assertOrderCreated(checkoutSessionId, user, scenario.store(), prevOrderCount);
    }

    @Test
    void paymentSuccess_whenOrderSessionDeletedFromRedis_successfullyCreatesOrder() throws Exception {
        CountableScenario scenario = saveCountableScenario(50);
        User user = cartTestSupport.saveUser(Role.USER);
        CheckoutAttempt checkoutAttempt = saveCheckoutAttempt(user, scenario, null);
        saveOrderSessionToRedis(checkoutAttempt, scenario, user);
        orderSessionStorage.remove(checkoutAttempt.getOrderSessionId());

        String checkoutSessionId = checkoutAttempt.getCheckoutSessionId();
        String paymentIntentId = "pi_test_" + UUID.randomUUID().toString().replace("-", "");

        long prevOrderCount = orderRepository.count();
        String payload = stripeWebhookTestSupport.buildCheckoutSessionCompletedPayload(
                checkoutSessionId,
                paymentIntentId,
                checkoutAttempt.getCheckoutAttemptId(),
                checkoutAttempt.getOrderSessionId()
        );
        String signature = stripeWebhookTestSupport.signPayload(payload, webhookSecret);

        try (MockedStatic<PaymentIntent> mocked = stripeWebhookTestSupport.mockPaymentIntentRetrieve(paymentIntentId)) {
            stripeWebhookTestSupport.postWebhook(mockMvc, payload, signature)
                    .andExpect(status().isOk());
        }

        assertOrderCreated(checkoutSessionId, user, scenario.store(), prevOrderCount);
    }

    @Test
    void paymentSuccess_whenRedisIsUnavailable_successfullyCreatesOrder() throws Exception {
        CountableScenario scenario = saveCountableScenario(50);
        User user = cartTestSupport.saveUser(Role.USER);
        CheckoutAttempt checkoutAttempt = saveCheckoutAttempt(user, scenario, null);
        String checkoutSessionId = checkoutAttempt.getCheckoutSessionId();
        String paymentIntentId = "pi_test_" + UUID.randomUUID().toString().replace("-", "");

        long prevOrderCount = orderRepository.count();
        String payload = stripeWebhookTestSupport.buildCheckoutSessionCompletedPayload(
                checkoutSessionId,
                paymentIntentId,
                checkoutAttempt.getCheckoutAttemptId(),
                checkoutAttempt.getOrderSessionId()
        );
        String signature = stripeWebhookTestSupport.signPayload(payload, webhookSecret);

        redisContainer().stop();

        try (MockedStatic<PaymentIntent> mocked = stripeWebhookTestSupport.mockPaymentIntentRetrieve(paymentIntentId)) {
            stripeWebhookTestSupport.postWebhook(mockMvc, payload, signature)
                    .andExpect(status().isOk());
        } finally {
            if (!redisContainer().isRunning()) {
                redisContainer().start();
            }
        }

        assertOrderCreated(checkoutSessionId, user, scenario.store(), prevOrderCount);
    }

    @Test
    void paymentSuccess_whenRedisDownDuringCartCleanup_stillCreatesOrder() throws Exception {
        CountableScenario scenario = saveCountableScenario(50);
        User user = cartTestSupport.saveUser(Role.USER);
        UUID cartSessionId = UUID.randomUUID();
        CheckoutAttempt checkoutAttempt = saveCheckoutAttempt(user, scenario, cartSessionId);
        String checkoutSessionId = checkoutAttempt.getCheckoutSessionId();
        String paymentIntentId = "pi_test_" + UUID.randomUUID().toString().replace("-", "");

        long prevOrderCount = orderRepository.count();
        String payload = stripeWebhookTestSupport.buildCheckoutSessionCompletedPayload(
                checkoutSessionId,
                paymentIntentId,
                checkoutAttempt.getCheckoutAttemptId(),
                checkoutAttempt.getOrderSessionId()
        );
        String signature = stripeWebhookTestSupport.signPayload(payload, webhookSecret);

        redisContainer().stop();

        try (MockedStatic<PaymentIntent> mocked = stripeWebhookTestSupport.mockPaymentIntentRetrieve(paymentIntentId)) {
            stripeWebhookTestSupport.postWebhook(mockMvc, payload, signature)
                    .andExpect(status().isInternalServerError());
        } finally {
            if (!redisContainer().isRunning()) {
                redisContainer().start();
            }
        }

        assertOrderCreated(checkoutSessionId, user, scenario.store(), prevOrderCount);
    }

    @Test
    void paymentSuccess_whenFirstWebhookFailsOnRetry_recoverAndCreatesOrder() throws Exception {
        CountableScenario scenario = saveCountableScenario(50);
        User user = cartTestSupport.saveUser(Role.USER);
        CheckoutAttempt checkoutAttempt = saveCheckoutAttempt(user, scenario, null);
        String checkoutSessionId = checkoutAttempt.getCheckoutSessionId();
        String paymentIntentId = "pi_test_" + UUID.randomUUID().toString().replace("-", "");

        long prevOrderCount = orderRepository.count();
        int prevStock = scenario.storeInventory().getCurrentStock();
        Long storeInventoryId = scenario.storeInventory().getId();

        String payload = stripeWebhookTestSupport.buildCheckoutSessionCompletedPayload(
                checkoutSessionId,
                paymentIntentId,
                checkoutAttempt.getCheckoutAttemptId(),
                checkoutAttempt.getOrderSessionId()
        );
        String signature = stripeWebhookTestSupport.signPayload(payload, webhookSecret);

        doThrow(new RuntimeException("simulated server crash"))
                .doCallRealMethod()
                .when(orderService).completePaidOrder(any(), eq(checkoutSessionId), any());

        try (MockedStatic<PaymentIntent> mocked = stripeWebhookTestSupport.mockPaymentIntentRetrieve(paymentIntentId)) {
            stripeWebhookTestSupport.postWebhook(mockMvc, payload, signature)
                    .andExpect(status().isInternalServerError());
        }

        assertThat(orderRepository.count()).isEqualTo(prevOrderCount);
        assertThat(orderRepository.findByCheckoutSessionId(checkoutSessionId)).isEmpty();
        assertThat(checkoutAttemptRepository.findById(checkoutAttempt.getCheckoutAttemptId()).orElseThrow()
                .getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(storeInventoryRepository.findById(storeInventoryId).orElseThrow().getCurrentStock())
                .isEqualTo(prevStock);

        try (MockedStatic<PaymentIntent> mocked = stripeWebhookTestSupport.mockPaymentIntentRetrieve(paymentIntentId)) {
            stripeWebhookTestSupport.postWebhook(mockMvc, payload, signature)
                    .andExpect(status().isOk());
        }

        assertOrderCreated(checkoutSessionId, user, scenario.store(), prevOrderCount);
        assertThat(checkoutAttemptRepository.findById(checkoutAttempt.getCheckoutAttemptId()).orElseThrow()
                .getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
    }

    private void assertOrderCreated(String checkoutSessionId, User user, Store store, long prevOrderCount) {
        assertThat(orderRepository.count()).isEqualTo(prevOrderCount + 1);
        assertThat(orderRepository.findByCheckoutSessionId(checkoutSessionId)).isPresent();

        Order order = orderRepository.findByCheckoutSessionId(checkoutSessionId).orElseThrow();
        assertThat(order.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getUser().getId()).isEqualTo(user.getId());
        assertThat(order.getStore().getId()).isEqualTo(store.getId());
    }

    private CheckoutAttempt saveCheckoutAttempt(User user, CountableScenario scenario, UUID cartSessionId) {
        UUID orderSessionId = UUID.randomUUID();
        UUID guestId = UUID.randomUUID();
        String checkoutSessionId = "cs_test_" + UUID.randomUUID().toString().replace("-", "");

        OrderSessionProduct orderSessionProduct = buildOrderSessionProduct(scenario);

        CheckoutAttempt checkoutAttempt = CheckoutAttempt.builder()
                .storeId(scenario.store().getId())
                .userId(user.getId())
                .totalPrice(BigDecimal.valueOf(5.99))
                .orderType(OrderType.DELIVERY)
                .taxAmount(BigDecimal.ZERO)
                .paymentStatus(PaymentStatus.PENDING)
                .contactInfo(new ContactInfo("Test", "User", user.getEmail(), "5121234567"))
                .addressInfo(new AddressInfo("123 Main", "Apt 1", "78701", "Austin, TX"))
                .guestId(guestId)
                .cartSessionId(cartSessionId)
                .orderSessionId(orderSessionId)
                .checkoutSessionId(checkoutSessionId)
                .orderRecord(List.of(orderSessionProduct))
                .build();

        return checkoutAttemptRepository.save(checkoutAttempt);
    }

    private void saveOrderSessionToRedis(CheckoutAttempt checkoutAttempt, CountableScenario scenario, User user) {
        OrderSession orderSession = OrderSession.builder()
                .sessionId(checkoutAttempt.getOrderSessionId())
                .storeId(scenario.store().getId())
                .userId(user.getId())
                .totalPrice(BigDecimal.valueOf(5.99))
                .orderType(OrderType.DELIVERY)
                .paymentStatus(PaymentStatus.PENDING)
                .taxAmount(BigDecimal.ZERO)
                .contactInfo(checkoutAttempt.getContactInfo())
                .addressInfo(checkoutAttempt.getAddressInfo())
                .orderSessionProducts(List.of(buildOrderSessionProduct(scenario)))
                .build();
        orderSessionStorage.save(orderSession);
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
            Store store = catalog.saveStore("Stripe Webhook Branch");
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

            return new CountableScenario(store, storeProduct, productOption, ingredient, storeInventory);
        });
    }
}
