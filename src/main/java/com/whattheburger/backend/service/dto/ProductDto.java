package com.whattheburger.backend.service.dto;

import com.whattheburger.backend.domain.Product;
import com.whattheburger.backend.domain.enums.CountType;
import com.whattheburger.backend.domain.enums.CustomRuleType;
import com.whattheburger.backend.domain.enums.MeasureType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@Builder
@NoArgsConstructor // deserialize
@Data // serialize
public class ProductDto {
    private Product product;
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
        private BigDecimal extraPrice; //productOption
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
        private Integer defaultSelection; //productOptionTrait
        private BigDecimal extraPrice; //productOptionTrait
        private Double extraCalories; //productOptionTrait
    }
}
