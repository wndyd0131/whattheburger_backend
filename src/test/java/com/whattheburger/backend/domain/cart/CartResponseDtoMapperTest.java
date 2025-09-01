package com.whattheburger.backend.domain.cart;

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
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

public class CartResponseDtoMapperTest {

    CartDtoMapper cartDtoMapper = new CartDtoMapper();

    @Test
    public void givenProperValidatedCartDtosAndCalculatedCartDto_whenMapToProcessedCartDto_thenReturnExpectedDto() {
        Product mockProduct = MockProductFactory.createMockProduct();
        CustomRule mockCustomRule = MockCustomRuleFactory.createMockCustomRule();
        Option mockOption = MockOptionFactory.createMockOption();
        ProductOption mockProductOption = MockOptionFactory.createMockProductOption(mockProduct, mockOption, mockCustomRule);
        OptionTrait mockOptionTrait = MockOptionTraitFactory.createMockOptionTrait();
        ProductOptionTrait mockProductOptionTrait = MockOptionTraitFactory.createMockProductOptionTrait(mockProductOption, mockOptionTrait);

        List<ValidatedCartDto> validatedCartDtos = List.of(
                ValidatedCartDto
                        .builder()
                        .validatedProduct(new ValidatedProduct(mockProduct, 1))
                        .validatedCustomRules(
                                List.of(
                                        ValidatedCustomRule
                                                .builder()
                                                .customRule(mockCustomRule)
                                                .validatedOptions(
                                                        List.of(
                                                                ValidatedOption
                                                                        .builder()
                                                                        .productOption(mockProductOption)
                                                                        .quantity(1)
                                                                        .isSelected(true)
                                                                        .validatedQuantity(null)
                                                                        .validatedTraits(
                                                                                List.of(
                                                                                        ValidatedTrait
                                                                                                .builder()
                                                                                                .productOptionTrait(mockProductOptionTrait)
                                                                                                .currentValue(1)
                                                                                                .build()
                                                                                )
                                                                        )
                                                                        .build()
                                                        )

                                                )
                                                .build()
                                )

                        )
                        .build()
        );

        CalculatedCartDto calculatedCartDto = CalculatedCartDto
                .builder()
                .productCalculatorDtos(
                        List.of(
                                ProductCalculatorDto
                                        .builder()
                                        .productId(1L)
                                        .basePrice(new BigDecimal("5.99"))
                                        .quantity(1)
                                        .customRuleCalculatorDtos(
                                                List.of(
                                                        CustomRuleCalculatorDto
                                                                .builder()
                                                                .customRuleId(1L)
                                                                .optionTotalPrice(BigDecimal.ZERO)
                                                                .optionCalculatorDtos(
                                                                        List.of(
                                                                                OptionCalculatorDto
                                                                                        .builder()
                                                                                        .productOptionId(1L)
                                                                                        .defaultQuantity(1)
                                                                                        .isDefault(true)
                                                                                        .isSelected(true)
                                                                                        .price(BigDecimal.ZERO)
                                                                                        .traitTotalPrice(BigDecimal.ZERO)
                                                                                        .traitCalculatorDtos(
                                                                                                List.of(
                                                                                                        TraitCalculatorDto
                                                                                                                .builder()
                                                                                                                .productOptionTraitId(1L)
                                                                                                                .price(BigDecimal.ZERO)
                                                                                                                .defaultSelection(0)
                                                                                                                .requestedSelection(1)
                                                                                                                .optionTraitType(OptionTraitType.BINARY)
                                                                                                                .build()
                                                                                                )
                                                                                        )
                                                                                        .build()
                                                                        )
                                                                )
                                                                .build()
                                                )
                                        )
                                        .build()
                        )
                )
                .build();
        ProcessedCartDto processedCartDto = cartDtoMapper.toProcessedCartDto(validatedCartDtos, calculatedCartDto);
        Assertions.assertNotNull(processedCartDto);
    }
}
