package com.whattheburger.backend.service;

import com.whattheburger.backend.controller.dto.store.StoreCustomRuleModifyRequest;
import com.whattheburger.backend.controller.dto.store.StoreOptionModifyRequest;
import com.whattheburger.backend.controller.dto.store.StoreProductModifyRequestDto;
import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.domain.enums.DeltaType;
import com.whattheburger.backend.domain.enums.ModifyType;
import com.whattheburger.backend.repository.*;
import com.whattheburger.backend.security.enums.Role;
import com.whattheburger.backend.service.dto.StoreProductReadByProductIdDto;
import com.whattheburger.backend.service.exception.StoreProductNotFoundException;
import com.whattheburger.backend.service.exception.StoreProductStoreMismatchException;
import com.whattheburger.backend.utils.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreProductServiceTest {

    private static final Long STORE_ID = 1L;
    private static final Long STORE_PRODUCT_ID = 1L;
    private static final BigDecimal OVERRIDE_PRODUCT_PRICE = BigDecimal.valueOf(7.99);
    private static final BigDecimal BASE_PRODUCT_PRICE = BigDecimal.valueOf(5.99);
    private static final BigDecimal OVERRIDE_DELTA_PRICE = BigDecimal.valueOf(4.99);

    @Mock
    ProductRepository productRepository;
    @Mock
    ProductOptionRepository productOptionRepository;
    @Mock
    ProductOptionTraitRepository productOptionTraitRepository;
    @Mock
    StoreRepository storeRepository;
    @Mock
    StoreProductRepository storeProductRepository;
    @Mock
    StoreOptionDeltaRepository storeOptionDeltaRepository;

    @InjectMocks
    StoreProductService storeProductService;

    Store baseStore;
    Product baseProduct;
    CustomRule baseCustomRule;
    Option baseOption;
    ProductOption baseProductOption;
    StoreProduct baseStoreProduct;

    @BeforeEach
    void setUp() {
        initBaselineFixtures();
        ReflectionTestUtils.setField(storeProductService, "s3PublicUrl", "https://cdn.example.com");
    }

    @Test
    @Disabled
    void givenValidRequest_whenSaveStoreProduct_thenStoreProductIsSavedAndReturned() {
        Long productId = 1L;
        List<Long> storeIds = List.of(STORE_ID);

        Store store = Store
                .builder()
                .id(STORE_ID)
                .address(new Address("Austin", "123 Street", "TX", "12345"))
                .branch("")
                .closeTime(LocalTime.of(20, 0))
                .coordinate(new Coordinate(0D, 0D))
                .houseNumber(1L)
                .openTime(LocalTime.of(7, 0))
                .owner(new User(
                        "Admin",
                        "istrator",
                        "512-123-4567",
                        "12345",
                        "test@gmail.com",
                        "1234",
                        Role.ADMIN
                ))
                .phoneNum("213-123-4567")
                .website("www.whattheburger.com/1")
                .build();

        StoreProduct storeProduct = new StoreProduct(store, baseProduct);
        when(productRepository.findById(1L)).thenReturn(Optional.of(baseProduct));
        when(storeRepository.findAllById(any(List.class))).thenReturn(List.of(store));
        when(storeProductRepository.saveAll(any(List.class))).thenReturn(List.of(storeProduct));

        storeProductService.registerProductToStores(productId, storeIds);
    }

    @Nested
    class ModifyProduct {

        @Test
        void whenOverrideRequest_savesOverrideDelta() {
            StoreProductModifyRequestDto request = MockStoreProductFactory.createStoreProductEditRequestDto();
            givenStoreProductForModifyExists();

            storeProductService.modifyProduct(STORE_ID, STORE_PRODUCT_ID, request);

            ArgumentCaptor<StoreOptionDelta> captor = ArgumentCaptor.forClass(StoreOptionDelta.class);
            verify(storeOptionDeltaRepository).save(captor.capture());
            StoreOptionDelta saved = captor.getValue();
            assertThat(saved.getDeltaType()).isEqualTo(DeltaType.OVERRIDE);
            assertThat(saved.getOverridePrice()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        void whenHideRequest_savesHiddenDelta() {
            StoreProductModifyRequestDto request = hideRequestDto();
            givenStoreProductForModifyExists();
            StoreOptionDelta existingDelta = overrideDelta(baseStoreProduct);
            when(storeOptionDeltaRepository.findByStoreProductIdAndProductOptionId(STORE_PRODUCT_ID, 1L))
                    .thenReturn(Optional.of(existingDelta));

            storeProductService.modifyProduct(STORE_ID, STORE_PRODUCT_ID, request);

            ArgumentCaptor<StoreOptionDelta> captor = ArgumentCaptor.forClass(StoreOptionDelta.class);
            verify(storeOptionDeltaRepository).save(captor.capture());
            StoreOptionDelta saved = captor.getValue();
            assertThat(saved.getDeltaType()).isEqualTo(DeltaType.HIDDEN);
            assertThat(saved.getOverridePrice()).isNull();
        }

        private void givenStoreProductForModifyExists() {
            when(storeProductRepository.findById(STORE_PRODUCT_ID)).thenReturn(Optional.of(baseStoreProduct));
            when(productOptionRepository.findByProductId(1L)).thenReturn(List.of(baseProductOption));
            when(storeOptionDeltaRepository.findByStoreProductIdAndProductOptionId(STORE_PRODUCT_ID, 1L))
                    .thenReturn(Optional.empty());
        }

        private StoreProductModifyRequestDto hideRequestDto() {
            StoreOptionModifyRequest hideRequest = StoreOptionModifyRequest
                    .builder()
                    .productOptionId(1L)
                    .modifyType(ModifyType.HIDE)
                    .optionTraitRequests(new ArrayList<>())
                    .build();
            return StoreProductModifyRequestDto
                    .builder()
                    .productPrice(OVERRIDE_PRODUCT_PRICE)
                    .customRuleRequests(List.of(
                            StoreCustomRuleModifyRequest.builder()
                                    .customRuleId(1L)
                                    .optionRequests(List.of(hideRequest))
                                    .build()
                    ))
                    .build();
        }
    }

    @Nested
    class GetProductById {

        @Test
        void whenStoreProductNotFound_throwsStoreProductNotFoundException() {
            when(storeProductRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> storeProductService.getProductById(STORE_ID, 999L))
                    .isInstanceOf(StoreProductNotFoundException.class);
        }

        @Test
        void whenNoDelta_returnsBaseExtraPrice() {
            givenStoreProductExists(storeProductWithOverridePrice());

            StoreProductReadByProductIdDto result = storeProductService.getProductById(STORE_ID, STORE_PRODUCT_ID);

            assertThat(result.getStoreProductId()).isEqualTo(STORE_PRODUCT_ID);
            assertThat(result.getProductName()).isEqualTo(baseProduct.getName());
            assertThat(result.getProductPrice()).isEqualByComparingTo(OVERRIDE_PRODUCT_PRICE);
            assertThat(result.getOptionResponses()).hasSize(1);
            assertThat(result.getOptionResponses().get(0).getExtraPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        void whenOverrideDelta_returnsOverridePrice() {
            givenStoreProductExists(storeProductWithOverrideDelta());

            StoreProductReadByProductIdDto result = storeProductService.getProductById(STORE_ID, STORE_PRODUCT_ID);

            assertThat(result.getStoreProductId()).isEqualTo(STORE_PRODUCT_ID);
            assertThat(result.getProductName()).isEqualTo(baseProduct.getName());
            assertThat(result.getProductPrice()).isEqualByComparingTo(OVERRIDE_PRODUCT_PRICE);
            assertThat(result.getOptionResponses()).hasSize(1);
            assertThat(result.getOptionResponses().get(0).getExtraPrice()).isEqualByComparingTo(OVERRIDE_DELTA_PRICE);
        }

        @Test
        void whenHiddenDelta_excludesOption() {
            givenStoreProductExists(storeProductWithHiddenDelta());

            StoreProductReadByProductIdDto result = storeProductService.getProductById(STORE_ID, STORE_PRODUCT_ID);

            assertThat(result.getOptionResponses()).isEmpty();
        }

        @Test
        void whenUrlStoreIdMismatch_throwsStoreProductStoreMismatchException() {
            givenStoreProductExists(storeProductWithOverridePrice());

            assertThatThrownBy(() -> storeProductService.getProductById(999L, STORE_PRODUCT_ID))
                    .isInstanceOf(StoreProductStoreMismatchException.class);
        }

        @Test
        void whenOverridePriceSet_returnsOverridePriceAsProductPrice() {
            givenStoreProductExists(storeProductWithOverridePrice());

            StoreProductReadByProductIdDto result = storeProductService.getProductById(STORE_ID, STORE_PRODUCT_ID);

            assertThat(result.getProductPrice()).isEqualByComparingTo(OVERRIDE_PRODUCT_PRICE);
        }

        @Test
        void whenOverridePriceNull_returnsProductBasePrice() {
            givenStoreProductExists(storeProductWithoutOverridePrice());

            StoreProductReadByProductIdDto result = storeProductService.getProductById(STORE_ID, STORE_PRODUCT_ID);

            assertThat(result.getProductPrice()).isEqualByComparingTo(BASE_PRODUCT_PRICE);
        }

        private void givenStoreProductExists(StoreProduct storeProduct) {
            when(storeProductRepository.findById(STORE_PRODUCT_ID)).thenReturn(Optional.of(storeProduct));
        }
    }

    private void initBaselineFixtures() {
        baseStore = MockStoreFactory.createStore();
        baseProduct = MockProductFactory.createMockProduct();
        baseCustomRule = MockCustomRuleFactory.createMockCustomRule();
        baseOption = MockOptionFactory.createMockOption();
        baseProductOption = MockOptionFactory.createMockProductOption(baseProduct, baseOption, baseCustomRule);
        baseStoreProduct = storeProductWithOverridePrice();
    }

    private StoreProduct storeProductWithOverridePrice() {
        return storeProductWithOverridePrice(new ArrayList<>());
    }

    private StoreProduct storeProductWithOverridePrice(List<StoreOptionDelta> deltas) {
        return MockStoreProductFactory.createStoreProduct(
                STORE_PRODUCT_ID,
                OVERRIDE_PRODUCT_PRICE,
                true,
                baseStore,
                baseProduct,
                deltas
        );
    }

    private StoreProduct storeProductWithoutOverridePrice() {
        return MockStoreProductFactory.createStoreProduct(
                STORE_PRODUCT_ID,
                null,
                true,
                baseStore,
                baseProduct,
                new ArrayList<>()
        );
    }

    private StoreProduct storeProductWithOverrideDelta() {
        return storeProductWithOverridePrice(List.of(overrideDelta()));
    }

    private StoreProduct storeProductWithHiddenDelta() {
        return storeProductWithOverridePrice(List.of(hiddenDelta()));
    }

    private StoreOptionDelta overrideDelta() {
        return overrideDelta(null);
    }

    private StoreOptionDelta overrideDelta(StoreProduct storeProduct) {
        StoreOptionDelta delta = new StoreOptionDelta(baseProductOption, storeProduct);
        delta.override(OVERRIDE_DELTA_PRICE, DeltaType.OVERRIDE);
        return delta;
    }

    private StoreOptionDelta hiddenDelta() {
        StoreOptionDelta delta = new StoreOptionDelta(baseProductOption, null);
        delta.override(null, DeltaType.HIDDEN);
        return delta;
    }
}
