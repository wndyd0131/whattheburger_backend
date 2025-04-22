package com.whataburger.whataburgerproject.controller.dto;

import com.whataburger.whataburgerproject.domain.enums.CustomRuleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Builder
@NoArgsConstructor // deserialize
@Data // serialize
public class ProductReadByProductIdResponseDto {
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
        private Integer defaultSelection; //productOptionTrait
        private Double extraPrice; //productOptionTrait
        private Double extraCalories; //productOptionTrait
    }
}
