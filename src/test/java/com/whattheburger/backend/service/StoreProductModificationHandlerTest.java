package com.whattheburger.backend.service;

import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.domain.enums.ModifyType;
import com.whattheburger.backend.repository.*;
import com.whattheburger.backend.service.store_product.*;
import com.whattheburger.backend.utils.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StoreProductModificationHandlerTest {

    @Mock
    ModificationStrategyResolver modificationStrategyResolver;
    @Mock
    OverrideStrategy overrideStrategy;
    @Mock
    HideStrategy hideStrategy;

    @InjectMocks
    StoreProductModificationHandler modificationHandler;

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
    public void givenOptionOverrideCommand_whenHandle_thenStrategyIsSuccessfullyExecuted() {
        OptionModificationCommand modificationCommand = new OptionModificationCommand(
                1L,
                1L,
                mockStoreProduct,
                true,
                2,
                9,
                BigDecimal.valueOf(4.99),
                2,
                ModifyType.OVERRIDE
        );

        Assertions.assertInstanceOf(OptionModificationCommand.class, modificationCommand);

        when(modificationStrategyResolver.resolve(any(ModifyType.class))).thenReturn(overrideStrategy);
        modificationHandler.handle(modificationCommand);
        verify(modificationStrategyResolver).resolve(any(ModifyType.class));
        verify(overrideStrategy).execute(any(OptionModificationCommand.class));
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
