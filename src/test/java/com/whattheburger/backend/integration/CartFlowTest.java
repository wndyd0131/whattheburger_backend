package com.whattheburger.backend.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whattheburger.backend.controller.dto.CategorizedStoreProductsReadDto;
import com.whattheburger.backend.controller.dto.cart.CartCreateRequestDto;
import com.whattheburger.backend.controller.dto.cart.CartResponseDto;
import com.whattheburger.backend.controller.dto.cart.CustomRuleRequest;
import com.whattheburger.backend.controller.dto.cart.OptionRequest;
import com.whattheburger.backend.controller.dto.cart.OptionTraitRequest;
import com.whattheburger.backend.controller.dto.cart.ProductResponseDto;
import com.whattheburger.backend.controller.dto.cart.QuantityDetailRequest;
import com.whattheburger.backend.domain.Product;
import com.whattheburger.backend.domain.Store;
import com.whattheburger.backend.domain.StoreProduct;
import com.whattheburger.backend.domain.cart.CartList;
import com.whattheburger.backend.integration.support.BaseIntegrationTest;
import com.whattheburger.backend.integration.support.CatalogSeeder;
import com.whattheburger.backend.repository.StoreProductRepository;
import com.whattheburger.backend.repository.StoreRepository;
import com.whattheburger.backend.repository.UserRepository;
import com.whattheburger.backend.service.dto.StoreProductReadByProductIdDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import jakarta.servlet.http.Cookie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the "add to cart" flow that walks a real HTTP client
 * through category list -> product detail -> {@code POST /cart}, backed by
 * MySQL and Redis Testcontainers.
 *
 * <p>Uses {@link TestInstance.Lifecycle#PER_CLASS} so the catalog can be seeded
 * exactly once via {@link CatalogSeeder#seedByNamesAndRegisterToAllStores(String...)}.
 * Seeding twice in the same Spring context would trip
 * {@code StoreProductAlreadyExistException} (see {@code CatalogSeeder} javadoc).</p>
 */
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CartFlowTest extends BaseIntegrationTest {

    /** Names of products seeded once per class; values are verified to exist in {@code seeds/catalog.json}. */
    static final String PRIMARY_PRODUCT_NAME = "Whattheburger";
    static final String SECONDARY_PRODUCT_NAME = "French Fries";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CatalogSeeder catalogSeeder;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StoreProductRepository storeProductRepository;

    @Autowired
    private RedisTemplate<String, CartList> cartRedisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /** Sorted store ids from {@code data-integration.sql}, captured after seeding for stable lookup. */
    private List<Long> storeIds;

    /**
     * Store id -> product name -> {@code storeProductId}. Built once in {@link #seedCatalog()}
     * so individual tests can pick a product without re-querying the DB.
     */
    private Map<Long, Map<String, Long>> storeProductIdByStoreAndName;

    /** Redis keys written during the current test, flushed in {@link #flushTestRedisKeys()}. */
    private final Set<String> touchedRedisKeys = new HashSet<>();

    @BeforeAll
    void seedCatalog() throws Exception {
        // seed products and register to all stores
        List<Product> seededProducts = catalogSeeder.seedByNamesAndRegisterToAllStores(
                PRIMARY_PRODUCT_NAME,
                SECONDARY_PRODUCT_NAME
        );

        // cache storeIds
        storeIds = storeRepository.findAll().stream()
                .map(Store::getId)
                .sorted()
                .toList();

        // get storeProducts for each store and build storeProductIdByStoreAndName map
        List<Long> seededProductIds = seededProducts.stream().map(Product::getId).toList();
        storeProductIdByStoreAndName = new LinkedHashMap<>();
        for (Long storeId : storeIds) {
            Map<String, Long> storeProductMap = new LinkedHashMap<>(); // productName -> storeProductId
            List<StoreProduct> storeProducts =
                    storeProductRepository.findByStoreIdAndProductIdIn(storeId, seededProductIds);
            for (StoreProduct sp : storeProducts) {
                storeProductMap.put(sp.getProduct().getName(), sp.getId());
            }
            storeProductIdByStoreAndName.put(storeId, storeProductMap); // storeId -> (productName -> storeProductId)
        }
    }

    @AfterEach
    void flushTestRedisKeys() {
        if (touchedRedisKeys.isEmpty()) {
            return;
        }
        for (String sessionKey : new ArrayList<>(touchedRedisKeys)) {
            String sessionId = stringRedisTemplate.opsForValue().get(sessionKey);
            if (sessionId != null) {
                cartRedisTemplate.delete(sessionId);
            }
            stringRedisTemplate.delete(sessionKey);
        }
        touchedRedisKeys.clear();
    }

    // --- helpers -------------------------------------------------------

    /**
     * Mirrors {@code CartService#getSessionKey} for the guest branch so persistence
     * assertions in tests don't duplicate the key-building logic.
     */
    protected String redisKey(Long storeId, UUID guestId) {
        return "cart:store:" + storeId + ":" + guestId;
    }

    /** Same layout as {@link #redisKey(Long, UUID)} but for an authenticated user's email. */
    protected String redisKey(Long storeId, String username) {
        return "cart:store:" + storeId + ":" + username;
    }

    /**
     * Loads {@link CartList} the same way as {@code RedisCartSessionStorage#load}:
     * the session key maps to an inner session UUID (plain string), and the
     * {@code CartList} is stored as JSON at that UUID key.
     */
    protected CartList loadCartListFromSessionKey(String sessionKey) {
        String sessionId = stringRedisTemplate.opsForValue().get(sessionKey);
        if (sessionId == null) {
            return null;
        }
        return cartRedisTemplate.opsForValue().get(sessionId);
    }

    /** Convenience: first seeded store id from {@code data-integration.sql}. */
    protected Long firstStoreId() {
        return storeIds.get(0);
    }

    /** Convenience: second seeded store id from {@code data-integration.sql}. */
    protected Long secondStoreId() {
        return storeIds.get(1);
    }

    protected Long storeProductId(Long storeId, String productName) {
        Long id = storeProductIdByStoreAndName.getOrDefault(storeId, Map.of()).get(productName);
        if (id == null) {
            throw new IllegalStateException(
                    "No seeded storeProductId for store=" + storeId + ", product=" + productName
            );
        }
        return id;
    }

    /**
     * Builds a {@link CartCreateRequestDto} that satisfies {@code CartValidator} by walking
     * the detail response and picking, per {@code customRule}:
     * <ul>
     *   <li>every option with {@code isDefault == true},</li>
     *   <li>that option's {@code defaultSelection} trait (if any),</li>
     *   <li>and its {@code isDefault} quantity detail (if any).</li>
     * </ul>
     */
    protected CartCreateRequestDto buildDefaultCartRequest(StoreProductReadByProductIdDto detail, int quantity) {
        /**
         * Create cart request with default options by walking the detail response
         */
        Map<Long, CustomRuleRequest> customRuleRequestsById = new LinkedHashMap<>();

        List<StoreProductReadByProductIdDto.OptionResponse> options =
                Optional.ofNullable(detail.getOptionResponses()).orElse(List.of());

        for (StoreProductReadByProductIdDto.OptionResponse option : options) {
            if (!Boolean.TRUE.equals(option.getIsDefault())) {
                continue;
            }
            StoreProductReadByProductIdDto.CustomRuleResponse ruleResponse = option.getCustomRuleResponse();
            if (ruleResponse == null) {
                continue;
            }
            Long customRuleId = ruleResponse.getCustomRuleId();

            CustomRuleRequest ruleRequest = customRuleRequestsById.computeIfAbsent(customRuleId, id ->
                    CustomRuleRequest.builder()
                            .customRuleId(id)
                            .optionRequests(new ArrayList<>())
                            .build()
            );

            List<OptionTraitRequest> traitRequests = new ArrayList<>();
            List<StoreProductReadByProductIdDto.OptionTraitResponse> traitResponses =
                    Optional.ofNullable(option.getOptionTraitResponses()).orElse(List.of());
            for (StoreProductReadByProductIdDto.OptionTraitResponse trait : traitResponses) {
                traitRequests.add(OptionTraitRequest.builder()
                        .productOptionTraitId(trait.getProductOptionTraitId())
                        .currentValue(trait.getDefaultSelection())
                        .build());
            }

            QuantityDetailRequest quantityDetailRequest = Optional.ofNullable(option.getQuantityDetailResponses())
                    .orElse(List.of())
                    .stream()
                    .filter(q -> Boolean.TRUE.equals(q.getIsDefault()))
                    .findFirst()
                    .map(q -> QuantityDetailRequest.builder().id(q.getId()).build())
                    .orElse(null);

            OptionRequest optionRequest = OptionRequest.builder()
                    .productOptionId(option.getProductOptionId())
                    .optionQuantity(option.getDefaultQuantity())
                    .isSelected(true)
                    .optionTraitRequests(traitRequests)
                    .quantityDetailRequest(quantityDetailRequest)
                    .build();

            ruleRequest.getOptionRequests().add(optionRequest);
        }

        List<CustomRuleRequest> customRuleRequests = new ArrayList<>(customRuleRequestsById.values());
        customRuleRequests.sort(Comparator.comparing(CustomRuleRequest::getCustomRuleId));

        return CartCreateRequestDto.builder()
                .storeProductId(detail.getStoreProductId())
                .customRuleRequests(customRuleRequests)
                .quantity(quantity)
                .build();
    }

    /**
     * Posts {@code body} to {@code /api/v1/store/{storeId}/cart} with a {@code guestId} cookie.
     * Records the expected Redis key so {@link #flushTestRedisKeys()} can clean up.
     */
    protected ResultActions postCart(Long storeId, UUID guestId, CartCreateRequestDto body) throws Exception {
        touchedRedisKeys.add(redisKey(storeId, guestId));
        return mockMvc.perform(cartRequest(storeId, body)
                .cookie(new Cookie("guestId", guestId.toString())));
    }

    /**
     * Authenticated variant of {@link #postCart(Long, UUID, CartCreateRequestDto)}; caller supplies
     * a pre-configured request post-processor (e.g. {@code SecurityMockMvcRequestPostProcessors.user(...)}).
     */
    protected ResultActions postCartAsUser(
            Long storeId,
            String username,
            CartCreateRequestDto body,
            org.springframework.test.web.servlet.request.RequestPostProcessor authPostProcessor
    ) throws Exception {
        touchedRedisKeys.add(redisKey(storeId, username));
        return mockMvc.perform(cartRequest(storeId, body).with(authPostProcessor));
    }

    private MockHttpServletRequestBuilder cartRequest(Long storeId, CartCreateRequestDto body) throws Exception {
        return post("/api/v1/store/{storeId}/cart", storeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
    }

    /** Pulls the {@code storeProductId} for {@code productName} out of the category list response. */
    private Long fetchStoreProductIdFromCategoryList(Long storeId, String productName) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/store/{storeId}/category/product", storeId))
                .andExpect(status().isOk())
                .andReturn();
        List<CategorizedStoreProductsReadDto> categories = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<List<CategorizedStoreProductsReadDto>>() {}
        );
        return categories.stream()
                .flatMap(c -> Optional.ofNullable(c.getProducts()).orElse(List.of()).stream())
                .filter(p -> productName.equals(p.getName()))
                .map(CategorizedStoreProductsReadDto.StoreProductDto::getStoreProductId)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "storeProductId for '" + productName + "' not found in category list of store " + storeId
                ));
    }

    /** GETs the product detail and deserializes the response. */
    private StoreProductReadByProductIdDto fetchStoreProductDetail(Long storeId, Long storeProductId) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/store/{storeId}/product/{storeProductId}", storeId, storeProductId))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readValue(
                result.getResponse().getContentAsString(),
                StoreProductReadByProductIdDto.class
        );
    }

    private CartResponseDto readCartResponse(MvcResult result) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsString(), CartResponseDto.class);
    }

    // Add to Cart

    @Test
    void guestFlow_fromCategoryListToAddToCart_returns201AndSingleItem() throws Exception {
        Long storeId = firstStoreId();
        UUID guestId = UUID.randomUUID(); // Create new guest

        Long storeProductIdFromList = fetchStoreProductIdFromCategoryList(storeId, PRIMARY_PRODUCT_NAME); // Select single storeProduct item from categories of the store
        assertThat(storeProductIdFromList).isEqualTo(storeProductId(storeId, PRIMARY_PRODUCT_NAME)); // Compare to real data

        StoreProductReadByProductIdDto detail = fetchStoreProductDetail(storeId, storeProductIdFromList); // Get storeProduct detail
        assertThat(detail.getStoreProductId()).isEqualTo(storeProductIdFromList); // Test if the detail belongs to the storeProduct

        CartCreateRequestDto body = buildDefaultCartRequest(detail, 1);

        MvcResult result = postCart(storeId, guestId, body)
                .andExpect(status().isCreated())
                .andReturn();

        CartResponseDto response = readCartResponse(result);
        assertThat(response.getProductResponses()).hasSize(1); // response count of product added to cart
        ProductResponseDto product = response.getProductResponses().get(0);
        assertThat(product.getStoreProductId()).isEqualTo(storeProductIdFromList); // validate product id is expected
        assertThat(product.getQuantity()).isEqualTo(1); // added product count
        assertThat(response.getCartTotalPrice()).isNotNull();
        assertThat(response.getCartTotalPrice().compareTo(BigDecimal.ZERO)).isPositive();

        CartList persisted = loadCartListFromSessionKey(redisKey(storeId, guestId));
        assertThat(persisted).isNotNull();
        assertThat(persisted.getCarts()).hasSize(1);
        assertThat(persisted.getCarts().get(0).getStoreProductId()).isEqualTo(storeProductIdFromList);
    }
    @Test
    void guestFlow_addToCartWhenCartSessionIsExpired_returns201AndSingleItem() throws Exception {
    }
    @Test
    void guestFlow_addToCartWhenOptionNotActive_returns400() throws Exception {
    }

    @Test
    void guestFlow_addToCartWhenOptionOutOfStock_returns400() throws Exception {
    }

    @Test
    void guestFlow_cartIsFull_returns400() throws Exception {

    }
    @Test
    void guestFlow_cartPriceExceedsLimit_returns400() throws Exception {
    }
    // Read from cart
    // Modify cart
    // Delete from Cart
}
