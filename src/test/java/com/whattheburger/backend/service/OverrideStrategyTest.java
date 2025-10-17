package com.whattheburger.backend.service;

import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.domain.enums.ModifyType;
import com.whattheburger.backend.repository.ProductOptionRepository;
import com.whattheburger.backend.repository.StoreOptionDeltaRepository;
import com.whattheburger.backend.service.store_product.ModificationStrategyResolver;
import com.whattheburger.backend.service.store_product.OptionModificationCommand;
import com.whattheburger.backend.service.store_product.OverrideStrategy;
import com.whattheburger.backend.utils.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OverrideStrategyTest {

    @Mock
    ProductOptionRepository productOptionRepository;
    @Mock
    StoreOptionDeltaRepository storeOptionDeltaRepository;

    @InjectMocks
    OverrideStrategy overrideStrategy;

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
    }
    @Test
    public void givenOptionModificationCommand_whenDeltaExists_thenDeltaIsSuccessfullyUpdated() throws Exception {
        OptionModificationCommand modificationCommand = new OptionModificationCommand(
                1L,
                1L,
                mockStoreProduct,
                true,
                2,
                9,
                BigDecimal.valueOf(4.99),
                4,
                ModifyType.OVERRIDE
        );
        StoreOptionDelta mockStoreOptionDelta = new StoreOptionDelta(
                mockProductOption,
                mockStoreProduct
        );
        when(productOptionRepository.findByProductIdAndOptionId(any(Long.class), any(Long.class))).thenReturn(Optional.ofNullable(mockProductOption));
        when(storeOptionDeltaRepository.findById(any(Long.class))).thenReturn(Optional.ofNullable(mockStoreOptionDelta));
        overrideStrategy.execute(modificationCommand);
        verify(storeOptionDeltaRepository).save(any(StoreOptionDelta.class));
    }

    @Test
    public void givenOptionModificationCommand_whenDeltaDoesNotExist_thenDeltaSuccessfullyCreated() throws Exception {
        OptionModificationCommand modificationCommand = new OptionModificationCommand(
                1L,
                1L,
                mockStoreProduct,
                true,
                2,
                9,
                BigDecimal.valueOf(4.99),
                4,
                ModifyType.OVERRIDE
        );
        StoreOptionDelta mockStoreOptionDelta = null;
        when(productOptionRepository.findByProductIdAndOptionId(any(Long.class), any(Long.class))).thenReturn(Optional.ofNullable(mockProductOption));
        when(storeOptionDeltaRepository.findById(any(Long.class))).thenReturn(Optional.ofNullable(mockStoreOptionDelta));
        overrideStrategy.execute(modificationCommand);
        verify(storeOptionDeltaRepository).save(any(StoreOptionDelta.class));
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
