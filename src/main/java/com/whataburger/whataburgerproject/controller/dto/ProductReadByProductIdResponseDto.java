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
    private double productPrice;
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
        private int defaultQuantity; //productOption
        private int maxQuantity; //productOption
        private double extraPrice; //productOption
        private double calories;
        private String imageSource;
        private int orderIndex;
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
        private int rowIndex;
        private int minSelection;
        private int maxSelection;
    }

    @AllArgsConstructor
    @Builder
    @NoArgsConstructor
    @Data
    public static class OptionTraitResponse {
        private Long optionTraitId;
        private String name;
        private int defaultSelection; //productOptionTrait
        private double extraPrice; //productOptionTrait
        private double extraCalories; //productOptionTrait
    }
}
