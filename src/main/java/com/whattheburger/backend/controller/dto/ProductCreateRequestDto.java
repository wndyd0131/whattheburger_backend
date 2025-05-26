package com.whattheburger.backend.controller.dto;
import com.whattheburger.backend.domain.Product;
import com.whattheburger.backend.domain.enums.CountType;
import com.whattheburger.backend.domain.enums.MeasureType;
import com.whattheburger.backend.domain.enums.CustomRuleType;
import com.whattheburger.backend.domain.enums.ProductType;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductCreateRequestDto {
    private String productName;
    private Double productPrice;
    private Double productCalories;
    private ProductType productType;
    private String briefInfo;
    private String imageSource;
    private List<Long> categoryIds;
    private List<CustomRuleRequest> customRuleRequests;

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class CustomRuleRequest {
        private String customRuleName;
        private CustomRuleType customRuleType;
        private Integer orderIndex;
        private Integer minSelection;
        private Integer maxSelection;
        private List<OptionRequest> optionRequests;
    }

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class OptionRequest {
        private Long optionId;
        private Boolean isDefault;
        private CountType countType;
        private MeasureType measureType;
        private Integer defaultQuantity;
        private Integer maxQuantity;
        private Double extraPrice;
        private Integer orderIndex;
        private List<OptionTraitRequest> optionTraitRequests;
    }

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class OptionTraitRequest {
        private Long optionTraitId;
        private Integer defaultSelection;
        private Double extraPrice;
        private Double extraCalories;
    }

    public Product toEntity() {
        return new Product(productName, productPrice, briefInfo, imageSource, productCalories, productType);
    }
}