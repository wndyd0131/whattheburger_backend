package com.whattheburger.backend.service.dto;

import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.domain.enums.CountType;
import com.whattheburger.backend.domain.enums.CustomRuleType;
import com.whattheburger.backend.domain.enums.MeasureType;

import com.whattheburger.backend.domain.enums.OptionTraitType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Builder
@NoArgsConstructor // deserialize
@Data // serialize
public class ProductReadByProductIdDto {
    private Long productId;
    private String productName;
    private Double productPrice;
    private String imageSource;
    private String briefInfo;
    private List<OptionResponse> optionResponses;

    @AllArgsConstructor
    @Builder
    @NoArgsConstructor
    @Data
    public static class OptionResponse {
        private Long optionId;
        private String name;
        private Boolean isDefault; //productOption
        private Integer defaultQuantity; //productOption
        private Integer maxQuantity; //productOption
        private Double extraPrice; //productOption
        private Double calories;
        private CountType countType;
        private MeasureType measureType;
        private String imageSource;
        private Integer orderIndex;
        private CustomRuleResponse customRuleResponse;
        private List<OptionTraitResponse> optionTraitResponses;
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
        private Long optionTraitId;
        private String name;
        private String labelCode;
        private OptionTraitType optionTraitType;
        private Integer defaultSelection; //productOptionTrait
        private Double extraPrice; //productOptionTrait
        private Double extraCalories; //productOptionTrait
    }

    public static ProductReadByProductIdDto toDto(Product product){
        List<ProductOption> productOptions = product.getProductOptions();
        List<OptionResponse> optionResponses = new ArrayList<>();

        for (ProductOption productOption : productOptions) {
            Option option = productOption.getOption();
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
                                .optionTraitId(optionTrait.getId())
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
                    .optionId(option.getId())
                    .name(option.getName())
                    .isDefault(productOption.getIsDefault())
                    .defaultQuantity(productOption.getDefaultQuantity())
                    .maxQuantity(productOption.getMaxQuantity())
                    .extraPrice(productOption.getExtraPrice())
                    .calories(option.getCalories())
                    .countType(productOption.getCountType())
                    .measureType(productOption.getMeasureType())
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
                .imageSource(product.getImageSource())
                .briefInfo(product.getBriefInfo())
                .optionResponses(optionResponses)
                .build();
    }
}
