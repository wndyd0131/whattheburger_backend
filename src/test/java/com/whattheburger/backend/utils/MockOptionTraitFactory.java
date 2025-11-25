package com.whattheburger.backend.utils;

import com.whattheburger.backend.domain.OptionTrait;
import com.whattheburger.backend.domain.ProductOption;
import com.whattheburger.backend.domain.ProductOptionTrait;
import com.whattheburger.backend.domain.enums.OptionTraitType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MockOptionTraitFactory {

    public static OptionTrait createMockOptionTrait() {
        return OptionTrait
                .builder()
                .id(1L)
                .name("Toast Both Sides")
                .labelCode("TBS")
                .optionTraitType(OptionTraitType.BINARY)
                .productOptionTraits(new ArrayList<>())
                .build();
    }

    public static OptionTrait createMockOptionTrait(
            Long traitId,
            String traitName,
            String labelCode,
            OptionTraitType optionTraitType
    ) {
                return OptionTrait
                .builder()
                .id(traitId)
                .name(traitName)
                .labelCode(labelCode)
                .optionTraitType(optionTraitType)
                .productOptionTraits(new ArrayList<>())
                .build();
    }

    public static ProductOptionTrait createMockProductOptionTrait(ProductOption mockProductOption, OptionTrait mockOptionTrait) {
        ProductOptionTrait productOptionTrait = ProductOptionTrait
                .builder()
                .id(1L)
                .productOption(mockProductOption)
                .optionTrait(mockOptionTrait)
                .defaultSelection(0)
                .extraPrice(BigDecimal.ZERO)
                .extraCalories(0D)
                .build();
        mockProductOption.getProductOptionTraits().add(productOptionTrait);
        mockOptionTrait.getProductOptionTraits().add(productOptionTrait);
        return productOptionTrait;
    }

    public static ProductOptionTrait createMockProductOptionTrait(
            Long productOptionTraitId,
            Integer defaultSelection,
            BigDecimal extraPrice,
            Double extraCalories,
            ProductOption mockProductOption,
            OptionTrait mockOptionTrait
    ) {
        ProductOptionTrait productOptionTrait = ProductOptionTrait
                .builder()
                .id(productOptionTraitId)
                .productOption(mockProductOption)
                .optionTrait(mockOptionTrait)
                .defaultSelection(defaultSelection)
                .extraPrice(extraPrice)
                .extraCalories(extraCalories)
                .build();
        mockProductOption.getProductOptionTraits().add(productOptionTrait);
        mockOptionTrait.getProductOptionTraits().add(productOptionTrait);
        return productOptionTrait;
    }
}
