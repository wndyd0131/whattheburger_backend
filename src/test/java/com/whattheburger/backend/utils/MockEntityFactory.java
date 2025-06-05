package com.whattheburger.backend.utils;

import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.domain.enums.*;

public class MockEntityFactory {

    public static Category createMockCategory() {
        return Category
                .builder()
                .id(1L)
                .name("Burgers")
                .build();
    }

    public static Option createMockOption() {
        return Option
                .builder()
                .id(1L)
                .name("Large Bun")
                .imageSource("")
                .calories(310D)
                .build();
    }

    public static OptionTrait createMockOptionTrait() {
        return OptionTrait
                .builder()
                .id(1L)
                .name("Toast Both Sides")
                .labelCode("TBS")
                .optionTraitType(OptionTraitType.BINARY)
                .build();
    }

    public static Product createMockProduct() {
        return Product
                .builder()
                .id(1L)
                .name("Whattheburger")
                .price(5.99D)
                .briefInfo("")
                .imageSource("")
                .calories(590D)
                .productType(ProductType.ONLY)
                .build();
    }

    public static CategoryProduct createMockCategoryProduct(Category mockCategory, Product mockProduct) {
        return CategoryProduct
                .builder()
                .id(1L)
                .category(mockCategory)
                .product(mockProduct)
                .build();
    }

    public static CustomRule createMockCustomRule() {
        return CustomRule
                .builder()
                .id(1L)
                .name("Bread")
                .customRuleType(CustomRuleType.UNIQUE)
                .orderIndex(0)
                .minSelection(1)
                .maxSelection(1)
                .build();
    }

    public static ProductOption createMockProductOption(Product mockProduct, Option mockOption, CustomRule mockCustomRule) {
        return ProductOption
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
                .extraPrice(0D)
                .orderIndex(0)
                .build();
    }

    public static ProductOptionTrait createMockProductOptionTrait(ProductOption mockProductOption, OptionTrait mockOptionTrait) {
        return ProductOptionTrait
                .builder()
                .id(1L)
                .productOption(mockProductOption)
                .optionTrait(mockOptionTrait)
                .defaultSelection(0)
                .extraPrice(0D)
                .extraCalories(0D)
                .build();
    }
}

