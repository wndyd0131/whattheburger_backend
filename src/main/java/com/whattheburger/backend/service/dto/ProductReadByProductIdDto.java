package com.whattheburger.backend.service.dto;

import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.domain.enums.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Builder
@NoArgsConstructor // deserialize
@Data // serialize
public class ProductReadByProductIdDto {
    private Long productId;
    private String productName;
    private BigDecimal productPrice;
    private Double productCalories;
    private String imageSource;
    private String briefInfo;
    private List<OptionResponse> optionResponses;

    @AllArgsConstructor
    @Builder
    @NoArgsConstructor
    @Data
    public static class OptionResponse {
        private Long productOptionId;
        private String name;
        private Boolean isDefault; //productOption
        private Integer defaultQuantity; //productOption
        private Integer maxQuantity; //productOption
        private BigDecimal extraPrice; //productOption
        private Double calories;
        private CountType countType;
        private String imageSource;
        private Integer orderIndex;
        private CustomRuleResponse customRuleResponse;
        private List<QuantityDetailResponse> quantityDetailResponses;
        private List<OptionTraitResponse> optionTraitResponses;
    }

    @AllArgsConstructor
    @Builder
    @NoArgsConstructor
    @Data
    public static class QuantityDetailResponse {
        private Long id;
        private QuantityType quantityType;
        private String labelCode;
        private BigDecimal extraPrice;
        private Double extraCalories;
        private Boolean isDefault; // ProductOptionOptionQuantity
    }

    @AllArgsConstructor
    @Builder
    @NoArgsConstructor
    @Data
    public static class CustomRuleResponse {
        private Long customRuleId;
        private String name;
        private CustomRuleType customRuleType;
        private Integer orderIndex;
        private Integer minSelection;
        private Integer maxSelection;
    }

    @AllArgsConstructor
    @Builder
    @NoArgsConstructor
    @Data
    public static class OptionTraitResponse {
        private Long productOptionTraitId;
        private String name;
        private String labelCode;
        private OptionTraitType optionTraitType;
        private Integer defaultSelection; //productOptionTrait
        private BigDecimal extraPrice; //productOptionTrait
        private Double extraCalories; //productOptionTrait
    }

    public static ProductReadByProductIdDto toDto(Product product, String imageSource){
        List<ProductOption> productOptions = product.getProductOptions();
        List<OptionResponse> optionResponses = new ArrayList<>();

        for (ProductOption productOption : productOptions) {
            Option option = productOption.getOption();
            List<QuantityDetailResponse> quantityDetailResponses = productOption.getProductOptionOptionQuantities()
                    .stream()
                    .map(productOptionQuantity -> QuantityDetailResponse
                            .builder()
                            .id(productOptionQuantity.getId())
                            .quantityType(productOptionQuantity.getOptionQuantity().getQuantity().getQuantityType())
                            .labelCode(productOptionQuantity.getOptionQuantity().getQuantity().getLabelCode())
                            .extraPrice(productOptionQuantity.getExtraPrice())
                            .extraCalories(productOptionQuantity.getOptionQuantity().getExtraCalories())
                            .isDefault(productOptionQuantity.getIsDefault())
                            .build()
                    )
                    .collect(Collectors.toList());

            List<ProductOptionTrait> productOptionTraits = productOption.getProductOptionTraits();
            List<OptionTraitResponse> optionTraitResponses = new ArrayList<>();
            CustomRule customRule = productOption.getCustomRule();
            CustomRuleResponse customRuleResponse =
                    CustomRuleResponse
                            .builder()
                            .customRuleId(customRule.getId())
                            .name(customRule.getName())
                            .customRuleType(customRule.getCustomRuleType())
                            .orderIndex(customRule.getOrderIndex())
                            .minSelection(customRule.getMinSelection())
                            .maxSelection(customRule.getMaxSelection())
                            .build();
            for (ProductOptionTrait productOptionTrait : productOptionTraits) {
                OptionTrait optionTrait = productOptionTrait.getOptionTrait();
                optionTraitResponses.add(
                        OptionTraitResponse
                                .builder()
                                .productOptionTraitId(productOptionTrait.getId())
                                .name(optionTrait.getName())
                                .labelCode(optionTrait.getLabelCode())
                                .optionTraitType(optionTrait.getOptionTraitType())
                                .defaultSelection(productOptionTrait.getDefaultSelection())
                                .extraPrice(productOptionTrait.getExtraPrice())
                                .extraCalories(productOptionTrait.getExtraCalories())
                                .build()
                );
            }
            optionResponses.add(OptionResponse
                    .builder()
                    .productOptionId(productOption.getId())
                    .name(option.getName())
                    .isDefault(productOption.getIsDefault())
                    .defaultQuantity(productOption.getDefaultQuantity())
                    .maxQuantity(productOption.getMaxQuantity())
                    .quantityDetailResponses(quantityDetailResponses)
                    .extraPrice(productOption.getExtraPrice())
                    .calories(option.getCalories())
                    .countType(productOption.getCountType())
                    .imageSource(option.getImageSource())
                    .orderIndex(productOption.getOrderIndex())
                    .customRuleResponse(customRuleResponse)
                    .optionTraitResponses(optionTraitResponses)
                    .build()
            );
        }
        return ProductReadByProductIdDto
                .builder()
                .productId(product.getId())
                .productName(product.getName())
                .productPrice(product.getPrice())
                .imageSource(imageSource)
                .briefInfo(product.getBriefInfo())
                .optionResponses(optionResponses)
                .build();
    }
}
