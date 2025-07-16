package com.whattheburger.backend.utils;

import com.whattheburger.backend.domain.enums.OptionTraitType;
import com.whattheburger.backend.service.dto.cart.calculator.*;

import java.util.List;

public class MockCalculatorDtoFactory {
    public static CalculatorDto createMockCalculatorDto(List<ProductDetail> productDetails) {
        return new CalculatorDto(productDetails);
    }
    public static ProductDetail createMockProductDetail(
            Double basePrice,
            Integer quantity,
            List<OptionDetail> optionDetails
    ) {
        return ProductDetail
                .builder()
                .basePrice(basePrice)
                .quantity(quantity)
                .optionDetails(optionDetails)
                .build();
    }
    public static OptionDetail createMockOptionDetail(
            Double price,
            QuantityDetail quantityDetail,
            List<TraitDetail> traitDetails
    ) {
        return OptionDetail
                .builder()
                .price(price)
                .quantityDetail(quantityDetail)
                .traitDetails(traitDetails)
                .build();
    }
    public static QuantityDetail createMockQuantityDetail(Double price) {
        return new QuantityDetail(price);
    }
    public static TraitDetail createMockTraitDetail(Double price, Integer value, OptionTraitType optionTraitType) {
        return TraitDetail
                .builder()
                .price(price)
                .value(value)
                .optionTraitType(optionTraitType)
                .build();
    }
}
