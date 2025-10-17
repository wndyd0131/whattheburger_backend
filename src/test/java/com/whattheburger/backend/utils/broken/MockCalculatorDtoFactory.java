package com.whattheburger.backend.utils.broken;

import com.whattheburger.backend.domain.enums.OptionTraitType;
import com.whattheburger.backend.service.dto.cart.calculator.*;

import java.math.BigDecimal;
import java.util.List;

public class MockCalculatorDtoFactory {
//
//    public static CalculatorDto createMockCalculatorDto() {
//        ProductCalculatorDto productCalculatorDto1 = ProductCalculatorDto
//                .builder()
//                .basePrice(new BigDecimal("5.49"))
//                .quantity(3)
//                .customRuleCalculatorDtos(
//                        List.of(
//                                CustomRuleCalculatorDto
//                                        .builder()
//                                        .customRuleId(1L)
//                                        .optionCalculatorDtos(
//                                                List.of(
//                                                        OptionCalculatorDto // Large bun
//                                                                .builder()
//                                                                .isSelected(true)
//                                                                .traitCalculatorDtos(
//                                                                        List.of(
//                                                                                TraitCalculatorDto
//                                                                                        .builder()
//                                                                                        .optionTraitType(OptionTraitType.BINARY)
//                                                                                        .defaultSelection(0)
//                                                                                        .requestedSelection(1)
//                                                                                        .price(BigDecimal.ZERO)
//                                                                                        .build()
//                                                                        )
//                                                                )
//                                                                .quantityCalculatorDto(null)
//                                                                .quantity(1)
//                                                                .price(BigDecimal.ZERO)
//                                                                .defaultQuantity(1)
//                                                                .isDefault(true)
//                                                                .build(),
//                                                        OptionCalculatorDto // Small bun
//                                                                .builder()
//                                                                .isSelected(false)
//                                                                .traitCalculatorDtos(
//                                                                        List.of(
//                                                                                TraitCalculatorDto
//                                                                                        .builder()
//                                                                                        .optionTraitType(OptionTraitType.BINARY)
//                                                                                        .defaultSelection(0)
//                                                                                        .requestedSelection(0)
//                                                                                        .price(BigDecimal.ZERO)
//                                                                                        .build()
//                                                                        )
//                                                                )
//                                                                .quantityCalculatorDto(null)
//                                                                .quantity(1) // fake
//                                                                .price(BigDecimal.ZERO)
//                                                                .defaultQuantity(1)
//                                                                .isDefault(false)
//                                                                .build(),
//                                                        OptionCalculatorDto // Texas Toast
//                                                                .builder()
//                                                                .isSelected(false)
//                                                                .traitCalculatorDtos(
//                                                                        List.of()
//                                                                )
//                                                                .quantityCalculatorDto(null)
//                                                                .quantity(0)
//                                                                .price(BigDecimal.ZERO)
//                                                                .defaultQuantity(1)
//                                                                .isDefault(false)
//                                                                .build(),
//                                                        OptionCalculatorDto // Beef
//                                                                .builder()
//                                                                .isSelected(true)
//                                                                .traitCalculatorDtos(
//                                                                        List.of()
//                                                                )
//                                                                .quantityCalculatorDto(null)
//                                                                .quantity(3)
//                                                                .price(BigDecimal.valueOf(2))
//                                                                .defaultQuantity(1)
//                                                                .isDefault(true)
//                                                                .build(),
//                                                        OptionCalculatorDto // American Cheese
//                                                                .builder()
//                                                                .isSelected(false)
//                                                                .traitCalculatorDtos(
//                                                                        List.of()
//                                                                )
//                                                                .quantityCalculatorDto(null)
//                                                                .quantity(0)
//                                                                .price(new BigDecimal("0.6"))
//                                                                .defaultQuantity(1)
//                                                                .isDefault(false)
//                                                                .build(),
//                                                        OptionCalculatorDto // Jack Cheese
//                                                                .builder()
//                                                                .isSelected(true)
//                                                                .traitCalculatorDtos(
//                                                                        List.of()
//                                                                )
//                                                                .quantityCalculatorDto(null)
//                                                                .quantity(4)
//                                                                .price(new BigDecimal("0.6"))
//                                                                .defaultQuantity(1)
//                                                                .isDefault(false)
//                                                                .build(),
//                                                        OptionCalculatorDto // Bacon Slices
//                                                                .builder()
//                                                                .isSelected(true)
//                                                                .traitCalculatorDtos(
//                                                                        List.of()
//                                                                )
//                                                                .quantityCalculatorDto(null)
//                                                                .quantity(5)
//                                                                .price(new BigDecimal("0.5"))
//                                                                .defaultQuantity(1)
//                                                                .isDefault(false)
//                                                                .build(),
//                                                        OptionCalculatorDto // Avocado
//                                                                .builder()
//                                                                .isSelected(false)
//                                                                .traitCalculatorDtos(
//                                                                        List.of()
//                                                                )
//                                                                .quantityCalculatorDto(null)
//                                                                .quantity(4) // fake
//                                                                .price(new BigDecimal("1.3"))
//                                                                .defaultQuantity(1)
//                                                                .isDefault(false)
//                                                                .build(),
//                                                        OptionCalculatorDto // Tomato
//                                                                .builder()
//                                                                .isSelected(true)
//                                                                .traitCalculatorDtos(
//                                                                        List.of()
//                                                                )
//                                                                .quantityCalculatorDto(
//                                                                        QuantityCalculatorDto
//                                                                                .builder()
//                                                                                .price(BigDecimal.ZERO)
//                                                                                .requestedId(1L)
//                                                                                .defaultId(1L)
//                                                                                .build()
//                                                                )
//                                                                .quantity(1)
//                                                                .price(BigDecimal.ZERO)
//                                                                .defaultQuantity(1)
//                                                                .isDefault(true)
//                                                                .build(),
//                                                        OptionCalculatorDto // Pickle
//                                                                .builder()
//                                                                .isSelected(true)
//                                                                .traitCalculatorDtos(
//                                                                        List.of()
//                                                                )
//                                                                .quantityCalculatorDto(
//                                                                        QuantityCalculatorDto
//                                                                                .builder()
//                                                                                .price(BigDecimal.ZERO)
//                                                                                .requestedId(1L)
//                                                                                .defaultId(2L)
//                                                                                .build()
//                                                                )
//                                                                .quantity(1)
//                                                                .price(BigDecimal.ZERO)
//                                                                .defaultQuantity(1)
//                                                                .isDefault(true)
//                                                                .build(),
//                                                        OptionCalculatorDto // Mustard
//                                                                .builder()
//                                                                .isSelected(true)
//                                                                .traitCalculatorDtos(
//                                                                        List.of()
//                                                                )
//                                                                .quantityCalculatorDto(
//                                                                        QuantityCalculatorDto
//                                                                                .builder()
//                                                                                .price(BigDecimal.ZERO)
//                                                                                .requestedId(1L)
//                                                                                .defaultId(1L)
//                                                                                .build()
//                                                                )
//                                                                .quantity(1)
//                                                                .price(BigDecimal.ZERO)
//                                                                .defaultQuantity(1)
//                                                                .isDefault(true)
//                                                                .build(),
//                                                        OptionCalculatorDto // Creamy Pepper Sauce
//                                                                .builder()
//                                                                .isSelected(true)
//                                                                .traitCalculatorDtos(
//                                                                        List.of()
//                                                                )
//                                                                .quantityCalculatorDto(
//                                                                        QuantityCalculatorDto
//                                                                                .builder()
//                                                                                .price(BigDecimal.valueOf(1))
//                                                                                .requestedId(2L)
//                                                                                .defaultId(1L)
//                                                                                .build()
//                                                                )
//                                                                .quantity(1)
//                                                                .price(BigDecimal.valueOf(1))
//                                                                .defaultQuantity(1)
//                                                                .isDefault(false)
//                                                                .build()
//                                                )
//                                        )
//                                        .optionTotalPrice(BigDecimal.ZERO)
//                                        .build()
//                        )
//                ).build();
//
//        return new CalculatorDto(
//                List.of(
//                        productCalculatorDto1,
//                        productCalculatorDto1
//                )
//        );
//    }

    public static List<ProductCalculatorDto> createMockProductCalcDetails() {
        ProductCalculatorDto productCalculatorDto1 = ProductCalculatorDto
                .builder()
                .basePrice(BigDecimal.valueOf(5.49))
                .customRuleCalculationResult(
                        CustomRuleCalculationResult
                                .builder()
                                .customRuleCalculationDetails(List.of())
                                .customRuleTotalPrice(BigDecimal.valueOf(2.99))
                                .build()
                )
                .quantity(3)
                .build(); // 25.44
        ProductCalculatorDto productCalculatorDto2 = ProductCalculatorDto
                .builder()
                .basePrice(BigDecimal.valueOf(9.99))
                .customRuleCalculationResult(
                        CustomRuleCalculationResult
                                .builder()
                                .customRuleCalculationDetails(List.of())
                                .customRuleTotalPrice(BigDecimal.valueOf(5.64))
                                .build()
                )
                .quantity(2) // 31.26
                .build();
        // total: $56.70
        return List.of(
                productCalculatorDto1,
                productCalculatorDto2
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
