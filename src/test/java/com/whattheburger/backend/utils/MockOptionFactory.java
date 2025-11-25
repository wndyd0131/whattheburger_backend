package com.whattheburger.backend.utils;

import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.domain.enums.CountType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MockOptionFactory {
    public static Option createMockOption() {
        return Option
                .builder()
                .id(1L)
                .name("Large Bun")
                .imageSource("")
                .calories(310D)
                .productOptions(new ArrayList<>())
                .build();
    }

    public static Option createMockOption(
            Long optionId,
            String optionName,
            String imageSource,
            Double calories
    ) {
        return Option
                .builder()
                .id(optionId)
                .name(optionName)
                .imageSource(imageSource)
                .calories(calories)
                .productOptions(new ArrayList<>())
                .build();
    }

    public static ProductOption createMockProductOption(Product mockProduct, Option mockOption, CustomRule mockCustomRule) {
        ProductOption productOption = ProductOption
                .builder()
                .id(1L)
                .option(mockOption)
                .product(mockProduct)
                .customRule(mockCustomRule)
                .isDefault(false)
                .countType(CountType.COUNTABLE)
                .defaultQuantity(0)
                .maxQuantity(0)
                .extraPrice(BigDecimal.ZERO)
                .orderIndex(0)
                .productOptionTraits(new ArrayList<>())
                .build();
        mockProduct.getProductOptions().add(productOption);
        mockOption.getProductOptions().add(productOption);
        mockCustomRule.getProductOptions().add(productOption);
        return productOption;
    }

    public static ProductOption createMockProductOption(
            Long productOptionId,
            Boolean isDefault,
            CountType countType,
            Integer defaultQuantity,
            Integer maxQuantity,
            BigDecimal extraPrice,
            Integer orderIndex,
            Product mockProduct,
            Option mockOption,
            CustomRule mockCustomRule
    ) {
        ProductOption productOption = ProductOption
                .builder()
                .id(productOptionId)
                .option(mockOption)
                .product(mockProduct)
                .customRule(mockCustomRule)
                .isDefault(isDefault)
                .countType(countType)
                .defaultQuantity(defaultQuantity)
                .maxQuantity(maxQuantity)
                .extraPrice(extraPrice)
                .orderIndex(orderIndex)
                .productOptionTraits(new ArrayList<>())
                .build();
        mockProduct.getProductOptions().add(productOption);
        mockOption.getProductOptions().add(productOption);
        mockCustomRule.getProductOptions().add(productOption);
        return productOption;
    }
}
