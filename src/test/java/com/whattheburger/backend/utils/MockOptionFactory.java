package com.whattheburger.backend.utils;

import com.whattheburger.backend.domain.CustomRule;
import com.whattheburger.backend.domain.Option;
import com.whattheburger.backend.domain.Product;
import com.whattheburger.backend.domain.ProductOption;
import com.whattheburger.backend.domain.enums.CountType;
import com.whattheburger.backend.domain.enums.MeasureType;

import java.math.BigDecimal;
import java.util.ArrayList;

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
    public static ProductOption createMockProductOption(Product mockProduct, Option mockOption, CustomRule mockCustomRule) {
        ProductOption productOption = ProductOption
                .builder()
                .id(1L)
                .option(mockOption)
                .product(mockProduct)
                .customRule(mockCustomRule)
                .isDefault(false)
                .countType(CountType.COUNTABLE)
                .measureType(MeasureType.COUNT)
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
}
