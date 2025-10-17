package com.whattheburger.backend.domain.broken.cart;

import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.domain.enums.OptionTraitType;
import com.whattheburger.backend.dto_mapper.CartDtoMapper;
import com.whattheburger.backend.service.dto.cart.*;
import com.whattheburger.backend.service.dto.cart.calculator.*;
import com.whattheburger.backend.utils.MockCustomRuleFactory;
import com.whattheburger.backend.utils.MockOptionFactory;
import com.whattheburger.backend.utils.MockOptionTraitFactory;
import com.whattheburger.backend.utils.MockProductFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

@Disabled
public class CartResponseDtoMapperTest {

    CartDtoMapper cartDtoMapper = new CartDtoMapper();

    @Test
    @Disabled("")
    public void givenProperValidatedCartDtosAndCalculatedCartDto_whenMapToProcessedCartDto_thenReturnExpectedDto() {
        Product mockProduct = MockProductFactory.createMockProduct();
        CustomRule mockCustomRule = MockCustomRuleFactory.createMockCustomRule();
        Option mockOption = MockOptionFactory.createMockOption();
        ProductOption mockProductOption = MockOptionFactory.createMockProductOption(mockProduct, mockOption, mockCustomRule);
        OptionTrait mockOptionTrait = MockOptionTraitFactory.createMockOptionTrait();
        ProductOptionTrait mockProductOptionTrait = MockOptionTraitFactory.createMockProductOptionTrait(mockProductOption, mockOptionTrait);
    }
}
