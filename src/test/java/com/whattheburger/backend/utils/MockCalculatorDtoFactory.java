package com.whattheburger.backend.utils;

import com.whattheburger.backend.domain.enums.OptionTraitType;
import com.whattheburger.backend.service.dto.cart.calculator.*;

import java.math.BigDecimal;
import java.util.List;

public class MockCalculatorDtoFactory {

    public static CalculatorDto createMockCalculatorDto() {
        ProductCalcDetail productCalcDetail1 = ProductCalcDetail
                .builder()
                .basePrice(new BigDecimal("5.49"))
                .quantity(3)
                .customRuleCalcDetails(
                        List.of(
                                CustomRuleCalcDetail
                                        .builder()
                                        .customRuleId(1L)
                                        .optionCalcDetails(
                                                List.of(
                                                        OptionCalcDetail // Large bun
                                                                .builder()
                                                                .isSelected(true)
                                                                .traitCalcDetails(
                                                                        List.of(
                                                                                TraitCalcDetail
                                                                                        .builder()
                                                                                        .optionTraitType(OptionTraitType.BINARY)
                                                                                        .defaultSelection(0)
                                                                                        .requestedSelection(1)
                                                                                        .price(BigDecimal.ZERO)
                                                                                        .build()
                                                                        )
                                                                )
                                                                .quantityCalcDetail(null)
                                                                .quantity(1)
                                                                .price(BigDecimal.ZERO)
                                                                .defaultQuantity(1)
                                                                .isDefault(true)
                                                                .build(),
                                                        OptionCalcDetail // Small bun
                                                                .builder()
                                                                .isSelected(false)
                                                                .traitCalcDetails(
                                                                        List.of(
                                                                                TraitCalcDetail
                                                                                        .builder()
                                                                                        .optionTraitType(OptionTraitType.BINARY)
                                                                                        .defaultSelection(0)
                                                                                        .requestedSelection(0)
                                                                                        .price(BigDecimal.ZERO)
                                                                                        .build()
                                                                        )
                                                                )
                                                                .quantityCalcDetail(null)
                                                                .quantity(1) // fake
                                                                .price(BigDecimal.ZERO)
                                                                .defaultQuantity(1)
                                                                .isDefault(false)
                                                                .build(),
                                                        OptionCalcDetail // Texas Toast
                                                                .builder()
                                                                .isSelected(false)
                                                                .traitCalcDetails(
                                                                        List.of()
                                                                )
                                                                .quantityCalcDetail(null)
                                                                .quantity(0)
                                                                .price(BigDecimal.ZERO)
                                                                .defaultQuantity(1)
                                                                .isDefault(false)
                                                                .build(),
                                                        OptionCalcDetail // Beef
                                                                .builder()
                                                                .isSelected(true)
                                                                .traitCalcDetails(
                                                                        List.of()
                                                                )
                                                                .quantityCalcDetail(null)
                                                                .quantity(3)
                                                                .price(BigDecimal.valueOf(2))
                                                                .defaultQuantity(1)
                                                                .isDefault(true)
                                                                .build(),
                                                        OptionCalcDetail // American Cheese
                                                                .builder()
                                                                .isSelected(false)
                                                                .traitCalcDetails(
                                                                        List.of()
                                                                )
                                                                .quantityCalcDetail(null)
                                                                .quantity(0)
                                                                .price(new BigDecimal("0.6"))
                                                                .defaultQuantity(1)
                                                                .isDefault(false)
                                                                .build(),
                                                        OptionCalcDetail // Jack Cheese
                                                                .builder()
                                                                .isSelected(true)
                                                                .traitCalcDetails(
                                                                        List.of()
                                                                )
                                                                .quantityCalcDetail(null)
                                                                .quantity(4)
                                                                .price(new BigDecimal("0.6"))
                                                                .defaultQuantity(1)
                                                                .isDefault(false)
                                                                .build(),
                                                        OptionCalcDetail // Bacon Slices
                                                                .builder()
                                                                .isSelected(true)
                                                                .traitCalcDetails(
                                                                        List.of()
                                                                )
                                                                .quantityCalcDetail(null)
                                                                .quantity(5)
                                                                .price(new BigDecimal("0.5"))
                                                                .defaultQuantity(1)
                                                                .isDefault(false)
                                                                .build(),
                                                        OptionCalcDetail // Avocado
                                                                .builder()
                                                                .isSelected(false)
                                                                .traitCalcDetails(
                                                                        List.of()
                                                                )
                                                                .quantityCalcDetail(null)
                                                                .quantity(4) // fake
                                                                .price(new BigDecimal("1.3"))
                                                                .defaultQuantity(1)
                                                                .isDefault(false)
                                                                .build(),
                                                        OptionCalcDetail // Tomato
                                                                .builder()
                                                                .isSelected(true)
                                                                .traitCalcDetails(
                                                                        List.of()
                                                                )
                                                                .quantityCalcDetail(
                                                                        QuantityCalcDetail
                                                                                .builder()
                                                                                .price(BigDecimal.ZERO)
                                                                                .requestedId(1L)
                                                                                .defaultId(1L)
                                                                                .build()
                                                                )
                                                                .quantity(1)
                                                                .price(BigDecimal.ZERO)
                                                                .defaultQuantity(1)
                                                                .isDefault(true)
                                                                .build(),
                                                        OptionCalcDetail // Pickle
                                                                .builder()
                                                                .isSelected(true)
                                                                .traitCalcDetails(
                                                                        List.of()
                                                                )
                                                                .quantityCalcDetail(
                                                                        QuantityCalcDetail
                                                                                .builder()
                                                                                .price(BigDecimal.ZERO)
                                                                                .requestedId(1L)
                                                                                .defaultId(2L)
                                                                                .build()
                                                                )
                                                                .quantity(1)
                                                                .price(BigDecimal.ZERO)
                                                                .defaultQuantity(1)
                                                                .isDefault(true)
                                                                .build(),
                                                        OptionCalcDetail // Mustard
                                                                .builder()
                                                                .isSelected(true)
                                                                .traitCalcDetails(
                                                                        List.of()
                                                                )
                                                                .quantityCalcDetail(
                                                                        QuantityCalcDetail
                                                                                .builder()
                                                                                .price(BigDecimal.ZERO)
                                                                                .requestedId(1L)
                                                                                .defaultId(1L)
                                                                                .build()
                                                                )
                                                                .quantity(1)
                                                                .price(BigDecimal.ZERO)
                                                                .defaultQuantity(1)
                                                                .isDefault(true)
                                                                .build(),
                                                        OptionCalcDetail // Creamy Pepper Sauce
                                                                .builder()
                                                                .isSelected(true)
                                                                .traitCalcDetails(
                                                                        List.of()
                                                                )
                                                                .quantityCalcDetail(
                                                                        QuantityCalcDetail
                                                                                .builder()
                                                                                .price(BigDecimal.valueOf(1))
                                                                                .requestedId(2L)
                                                                                .defaultId(1L)
                                                                                .build()
                                                                )
                                                                .quantity(1)
                                                                .price(BigDecimal.valueOf(1))
                                                                .defaultQuantity(1)
                                                                .isDefault(false)
                                                                .build()
                                                )
                                        )
                                        .optionTotalPrice(BigDecimal.ZERO)
                                        .build()
                        )
                ).build();

        return new CalculatorDto(
                List.of(
                        productCalcDetail1,
                        productCalcDetail1
                )
        );
    }

    public static List<ProductCalcDetail> createMockProductCalcDetails() {
        ProductCalcDetail productCalcDetail1 = ProductCalcDetail
                .builder()
                .basePrice(BigDecimal.valueOf(5.49))
                .customRuleCalcDetails(List.of())
                .customRuleTotalPrice(BigDecimal.valueOf(2.99))
                .quantity(3)
                .build(); // 25.44
        ProductCalcDetail productCalcDetail2 = ProductCalcDetail
                .builder()
                .basePrice(BigDecimal.valueOf(9.99))
                .customRuleCalcDetails(List.of())
                .customRuleTotalPrice(BigDecimal.valueOf(5.64))
                .quantity(2) // 31.26
                .build();
        // total: $56.70
        return List.of(
                productCalcDetail1,
                productCalcDetail2
        );
    }

//    public static CalculatorDto createMockCalculatorDto(List<ProductDetail> productDetails) {
//        return new CalculatorDto(productDetails);
//    }
//    public static ProductDetail createMockProductDetail(
//            Double basePrice,
//            Integer quantity,
//            List<OptionDetail> optionDetails
//    ) {
//        return ProductDetail
//                .builder()
//                .basePrice(basePrice)
//                .quantity(quantity)
//                .optionDetails(optionDetails)
//                .build();
//    }
//    public static OptionDetail createMockOptionDetail(
//            Double price,
//            QuantityDetail quantityDetail,
//            List<TraitDetail> traitDetails
//    ) {
//        return OptionDetail
//                .builder()
//                .price(price)
//                .quantityDetail(quantityDetail)
//                .traitDetails(traitDetails)
//                .build();
//    }
//    public static QuantityDetail createMockQuantityDetail(Double price) {
//        return new QuantityDetail(price);
//    }
//    public static TraitDetail createMockTraitDetail(Double price, Integer value, OptionTraitType optionTraitType) {
//        return TraitDetail
//                .builder()
//                .price(price)
//                .value(value)
//                .optionTraitType(optionTraitType)
//                .build();
//    }

}
