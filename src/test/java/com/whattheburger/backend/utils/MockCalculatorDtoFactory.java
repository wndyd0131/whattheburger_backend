package com.whattheburger.backend.utils;

import com.whattheburger.backend.domain.enums.OptionTraitType;
import com.whattheburger.backend.service.dto.cart.calculator.*;
import org.springframework.boot.context.properties.bind.Name;

import java.math.BigDecimal;
import java.util.List;

public class MockCalculatorDtoFactory {

    public static CalculatorDto createMockCalculatorDto() {
        ProductDetail productDetail1 = ProductDetail
                .builder()
                .basePrice(new BigDecimal("5.49"))
                .quantity(3)
                .optionDetails(
                        List.of(
                                OptionDetail // Large bun
                                        .builder()
                                        .isSelected(true)
                                        .traitDetails(
                                                List.of(
                                                        TraitDetail
                                                                .builder()
                                                                .optionTraitType(OptionTraitType.BINARY)
                                                                .defaultSelection(0)
                                                                .requestedSelection(1)
                                                                .price(BigDecimal.ZERO)
                                                                .build()
                                                )
                                        )
                                        .quantityDetail(null)
                                        .quantity(1)
                                        .price(BigDecimal.ZERO)
                                        .defaultQuantity(1)
                                        .isDefault(true)
                                        .build(),
                                OptionDetail // Small bun
                                        .builder()
                                        .isSelected(false)
                                        .traitDetails(
                                                List.of(
                                                        TraitDetail
                                                                .builder()
                                                                .optionTraitType(OptionTraitType.BINARY)
                                                                .defaultSelection(0)
                                                                .requestedSelection(0)
                                                                .price(BigDecimal.ZERO)
                                                                .build()
                                                )
                                        )
                                        .quantityDetail(null)
                                        .quantity(1) // fake
                                        .price(BigDecimal.ZERO)
                                        .defaultQuantity(1)
                                        .isDefault(false)
                                        .build(),
                                OptionDetail // Texas Toast
                                        .builder()
                                        .isSelected(false)
                                        .traitDetails(
                                                List.of()
                                        )
                                        .quantityDetail(null)
                                        .quantity(0)
                                        .price(BigDecimal.ZERO)
                                        .defaultQuantity(1)
                                        .isDefault(false)
                                        .build(),
                                OptionDetail // Beef
                                        .builder()
                                        .isSelected(true)
                                        .traitDetails(
                                                List.of()
                                        )
                                        .quantityDetail(null)
                                        .quantity(3)
                                        .price(BigDecimal.valueOf(2))
                                        .defaultQuantity(1)
                                        .isDefault(true)
                                        .build(),
                                OptionDetail // American Cheese
                                        .builder()
                                        .isSelected(false)
                                        .traitDetails(
                                                List.of()
                                        )
                                        .quantityDetail(null)
                                        .quantity(0)
                                        .price(new BigDecimal("0.6"))
                                        .defaultQuantity(1)
                                        .isDefault(false)
                                        .build(),
                                OptionDetail // Jack Cheese
                                        .builder()
                                        .isSelected(true)
                                        .traitDetails(
                                                List.of()
                                        )
                                        .quantityDetail(null)
                                        .quantity(4)
                                        .price(new BigDecimal("0.6"))
                                        .defaultQuantity(1)
                                        .isDefault(false)
                                        .build(),
                                OptionDetail // Bacon Slices
                                        .builder()
                                        .isSelected(true)
                                        .traitDetails(
                                                List.of()
                                        )
                                        .quantityDetail(null)
                                        .quantity(5)
                                        .price(new BigDecimal("0.5"))
                                        .defaultQuantity(1)
                                        .isDefault(false)
                                        .build(),
                                OptionDetail // Avocado
                                        .builder()
                                        .isSelected(false)
                                        .traitDetails(
                                                List.of()
                                        )
                                        .quantityDetail(null)
                                        .quantity(4) // fake
                                        .price(new BigDecimal("1.3"))
                                        .defaultQuantity(1)
                                        .isDefault(false)
                                        .build(),
                                OptionDetail // Tomato
                                        .builder()
                                        .isSelected(true)
                                        .traitDetails(
                                                List.of()
                                        )
                                        .quantityDetail(
                                                QuantityDetail
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
                                OptionDetail // Pickle
                                        .builder()
                                        .isSelected(true)
                                        .traitDetails(
                                                List.of()
                                        )
                                        .quantityDetail(
                                                QuantityDetail
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
                                OptionDetail // Mustard
                                        .builder()
                                        .isSelected(true)
                                        .traitDetails(
                                                List.of()
                                        )
                                        .quantityDetail(
                                                QuantityDetail
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
                                OptionDetail // Creamy Pepper Sauce
                                        .builder()
                                        .isSelected(true)
                                        .traitDetails(
                                                List.of()
                                        )
                                        .quantityDetail(
                                                QuantityDetail
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
                .build(); // $16.39
        return new CalculatorDto(
                List.of(
                        productDetail1,
                        productDetail1
                )
        );
    }

    public static List<ProductDetail> createMockProductDetails() {
        ProductDetail productDetail1 = ProductDetail
                .builder()
                .basePrice(BigDecimal.valueOf(5.49))
                .optionDetails(List.of())
                .quantity(3)
                .build();
        ProductDetail productDetail2 = ProductDetail
                .builder()
                .basePrice(BigDecimal.valueOf(9.99))
                .optionDetails(List.of())
                .quantity(2)
                .build();
        // total: $36.45
        return List.of(
                productDetail1,
                productDetail2
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
