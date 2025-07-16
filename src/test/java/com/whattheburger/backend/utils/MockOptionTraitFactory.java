package com.whattheburger.backend.utils;

import com.whattheburger.backend.domain.OptionTrait;
import com.whattheburger.backend.domain.ProductOption;
import com.whattheburger.backend.domain.ProductOptionTrait;
import com.whattheburger.backend.domain.enums.OptionTraitType;

import java.util.ArrayList;

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
    public static ProductOptionTrait createMockProductOptionTrait(ProductOption mockProductOption, OptionTrait mockOptionTrait) {
        ProductOptionTrait productOptionTrait = ProductOptionTrait
                .builder()
                .id(1L)
                .productOption(mockProductOption)
                .optionTrait(mockOptionTrait)
                .defaultSelection(0)
                .extraPrice(0D)
                .extraCalories(0D)
                .build();
        mockProductOption.getProductOptionTraits().add(productOptionTrait);
        mockOptionTrait.getProductOptionTraits().add(productOptionTrait);
        return productOptionTrait;
    }
}
