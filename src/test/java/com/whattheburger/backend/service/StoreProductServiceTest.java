package com.whattheburger.backend.service;

import com.whattheburger.backend.controller.dto.store.StoreProductModifyRequestDto;
import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.domain.enums.ModifyType;
import com.whattheburger.backend.repository.*;
import com.whattheburger.backend.security.enums.Role;
import com.whattheburger.backend.service.store_product.*;
import com.whattheburger.backend.utils.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StoreProductServiceTest {

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
    @Mock
    StoreProductModificationHandler storeProductModificationHandler;
    @Mock
    ModificationStrategyResolver modificationStrategyResolver;
    @Mock
    OverrideStrategy overrideStrategy;
    @Mock
    HideStrategy hideStrategy;

    @InjectMocks
    StoreProductService storeProductService;

    Category mockCategory;
    Option mockOption;
    OptionTrait mockOptionTrait;
    Product mockProduct;
    Store mockStore;
    StoreProduct mockStoreProduct;
    CustomRule mockCustomRule;
    CategoryProduct mockCategoryProduct;
    ProductOption mockProductOption;
    ProductOptionTrait mockProductOptionTrait;


    @BeforeEach
    void setUp() {
        initMock();
        when(overrideStrategy.getSupportedType()).thenReturn(ModifyType.OVERRIDE);
        when(hideStrategy.getSupportedType()).thenReturn(ModifyType.HIDE);
        modificationStrategyResolver = new ModificationStrategyResolver(
                List.of(overrideStrategy, hideStrategy)
        );
    }

    @Test
    @Disabled
    public void givenValidRequest_whenSaveStoreProduct_thenStoreProductIsSavedAndReturned() {
        Long productId = 1L;
        List<Long> storeIds = List.of(1L);

        Store mockStore = Store
                .builder()
                .id(1L)
                .address(
                        new Address(
                                "Austin",
                                "123 Street",
                                "TX",
                                "12345"
                        )
                )
                .branch("")
                .closeTime(LocalTime.of(20, 0)) // open days (openAt, closeAt), open time (openAt, closeAt)
                .coordinate(new Coordinate(
                        0D,
                        0D
                ))
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

        StoreProduct storeProduct = new StoreProduct(
                mockStore,
                mockProduct
        );
        when(productRepository.findById(1L)).thenReturn(Optional.ofNullable(mockProduct));
        when(storeRepository.findAllById(any(List.class))).thenReturn(List.of(mockStore));
        when(storeProductRepository.saveAll(any(List.class))).thenReturn(List.of(storeProduct));
        storeProductService.registerProductToStores(productId, storeIds);

    }

    @Test
    public void givenOptionOverrideRequest_whenModifyStoreProduct_thenModificationHandlerIsSuccessfullyCalled() {
        StoreProductModifyRequestDto productModifyRequestDto = MockStoreProductFactory.createStoreProductEditRequestDto();
        when(storeProductRepository.findByStoreIdAndProductId(any(Long.class), any(Long.class))).thenReturn(Optional.ofNullable(mockStoreProduct));
        when(productOptionRepository.findByProductId(any(Long.class))).thenReturn(List.of(mockProductOption));
        when(productOptionTraitRepository.findByProductOptionProductId(any(Long.class))).thenReturn(List.of(mockProductOptionTrait));
        storeProductService.modifyProduct(1L, 1L, productModifyRequestDto);

        verify(storeProductModificationHandler).handle(any(OptionModificationCommand.class));
    }

    private void initMock() {
        mockCategory = MockCategoryFactory.createMockCategory();
        mockOption = MockOptionFactory.createMockOption();
        mockOptionTrait = MockOptionTraitFactory.createMockOptionTrait();
        mockProduct = MockProductFactory.createMockProduct();
        mockCustomRule = MockCustomRuleFactory.createMockCustomRule();
        mockCategoryProduct = MockProductFactory.createMockCategoryProduct(mockCategory, mockProduct);
        mockProductOption = MockOptionFactory.createMockProductOption(mockProduct, mockOption, mockCustomRule);
        mockProductOptionTrait = MockOptionTraitFactory.createMockProductOptionTrait(mockProductOption, mockOptionTrait);
        mockStore = MockStoreFactory.createStore();
        mockStoreProduct = MockStoreProductFactory.createStoreProduct(mockStore, mockProduct);
    }
}
