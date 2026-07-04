package com.whattheburger.backend.integration;

import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.domain.enums.*;
import com.whattheburger.backend.integration.support.BaseIntegrationTest;
import com.whattheburger.backend.repository.*;
import com.whattheburger.backend.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class LoadProductFlowTest extends BaseIntegrationTest {

    private static final BigDecimal BASE_EXTRA_PRICE = BigDecimal.valueOf(1.00);
    private static final BigDecimal OVERRIDE_DELTA_PRICE = BigDecimal.valueOf(2.50);

    @Autowired
    MockMvc mockMvc;

    @MockBean
    S3Service s3Service;

    @Autowired
    StoreRepository storeRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    OptionRepository optionRepository;
    @Autowired
    CustomRuleRepository customRuleRepository;
    @Autowired
    ProductOptionRepository productOptionRepository;
    @Autowired
    OptionTraitRepository optionTraitRepository;
    @Autowired
    ProductOptionTraitRepository productOptionTraitRepository;
    @Autowired
    StoreProductRepository storeProductRepository;
    @Autowired
    StoreOptionDeltaRepository storeOptionDeltaRepository;

    ProductOption productOption;
    Store store;

    @BeforeEach
    void setUp() {
        productOption = saveBurgerWithOptions();
        store = saveStore("Branch 1");
    }

    @Test
    void getProduct_whenNoDelta_returnsBaseExtraPrice() throws Exception {
        StoreProduct storeProduct = saveStoreProduct(store, productOption.getProduct());

        mockMvc.perform(get(
                        "/api/v1/store/{storeId}/product/{storeProductId}",
                        store.getId(),
                        storeProduct.getId()
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Burger"))
                .andExpect(jsonPath("$.optionResponses[0].extraPrice").value(1.00));
    }

    @Test
    void getProduct_whenOverrideDelta_returnsOverriddenExtraPrice() throws Exception {
        StoreProduct storeProduct = saveStoreProduct(store, productOption.getProduct());
        saveOverrideDelta(storeProduct, productOption);

        mockMvc.perform(get(
                        "/api/v1/store/{storeId}/product/{storeProductId}",
                        store.getId(),
                        storeProduct.getId()
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Burger"))
                .andExpect(jsonPath("$.optionResponses[0].extraPrice").value(2.50));
    }

    @Test
    void getProduct_whenStoreProductNotFound_returns404() throws Exception {
        mockMvc.perform(get(
                        "/api/v1/store/{storeId}/product/{storeProductId}",
                        store.getId(),
                        999_999L
                ))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProduct_whenStoreIdMismatch_returns400() throws Exception {
        StoreProduct storeProduct = saveStoreProduct(store, productOption.getProduct());
        Store otherStore = saveStore("Branch 2");

        mockMvc.perform(get(
                        "/api/v1/store/{storeId}/product/{storeProductId}",
                        otherStore.getId(),
                        storeProduct.getId()
                ))
                .andExpect(status().isBadRequest());
    }

    private ProductOption saveBurgerWithOptions() {
        Product product = productRepository.save(
                new Product("Burger", BigDecimal.valueOf(5.99), "brief", 590D, ProductType.ONLY)
        );
        Option option = optionRepository.save(
                new Option("Large Bun", "/img/bun.jpg", 310D)
        );
        CustomRule customRule = customRuleRepository.save(
                new CustomRule("Bread", CustomRuleType.UNIQUE, 0, 1, 1)
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
                        BASE_EXTRA_PRICE,
                        0
                )
        );
        OptionTrait optionTrait = optionTraitRepository.save(
                new OptionTrait("Toast Both Sides", "TBS", OptionTraitType.BINARY)
        );
        productOptionTraitRepository.save(
                new ProductOptionTrait(productOption, optionTrait, 0, BigDecimal.ZERO, 0D)
        );
        return productOption;
    }

    private Store saveStore(String branch) {
        return storeRepository.save(createStore(branch));
    }

    private StoreProduct saveStoreProduct(Store store, Product product) {
        return storeProductRepository.save(new StoreProduct(store, product));
    }

    private void saveOverrideDelta(StoreProduct storeProduct, ProductOption productOption) {
        StoreOptionDelta delta = new StoreOptionDelta(productOption, storeProduct);
        delta.override(OVERRIDE_DELTA_PRICE, DeltaType.OVERRIDE);
        storeOptionDeltaRepository.save(delta);
    }

    private Store createStore(String branch) {
        return Store.builder()
                .branch(branch)
                .houseNumber(1L)
                .phoneNum("512-123-4567")
                .website("www.whattheburger.com")
                .address(new Address("Austin", "123 Main St", "TX", "78701"))
                .coordinate(new Coordinate(30.0, -97.0))
                .build();
    }
}
