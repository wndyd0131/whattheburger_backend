package com.whataburger.whataburgerproject.controller.dto;
import com.whataburger.whataburgerproject.domain.Product;
import com.whataburger.whataburgerproject.domain.enums.MeasureType;
import com.whataburger.whataburgerproject.domain.enums.CustomRuleType;
import com.whataburger.whataburgerproject.domain.enums.ProductType;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductCreateRequestDto {
    private String productName;
    private double productPrice;
    private double calories;
    private ProductType productType;
    private String briefInfo;
    private String imageSource;
    private Long categoryId;
    private List<CustomRuleRequest> customRuleRequests;

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class CustomRuleRequest {
        private String customRuleName;
        private CustomRuleType customRuleType;
        private int rowIndex;
        private int minSelection;
        private int maxSelection;
        private List<OptionRequest> optionRequests;
    }

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class OptionRequest {
        private Long optionId;
        private Boolean isDefault;
        private MeasureType measureType;
        private int defaultQuantity;
        private int maxQuantity;
        private double extraPrice;
        private int orderIndex;
        private List<OptionTraitRequest> optionTraitRequests;
    }

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class OptionTraitRequest {
        private Long optionTraitId;
        private int defaultSelection;
        private int extraPrice;
        private int extraCalories;
    }

    public Product toEntity() {
        return new Product(productName, productPrice, briefInfo, imageSource, calories, productType);
    }
}