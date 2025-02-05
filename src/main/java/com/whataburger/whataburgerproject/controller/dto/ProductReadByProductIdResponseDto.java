package com.whataburger.whataburgerproject.controller.dto;

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
    private String ingredientInfo;
    private List<OptionRequest> optionRequests;

    @AllArgsConstructor
    @Builder
    @NoArgsConstructor
    @Data
    public static class OptionRequest {
        private Long optionId;
        private String name;
        private Boolean isDefault; //productOption
        private int defaultQuantity; //productOption
        private int maxQuantity; //productOption
        private double extraPrice; //productOption
        private int calories;
        private String imageSource;
        private List<OptionTraitRequest> optionTraitRequests;
    }

    @AllArgsConstructor
    @Builder
    @NoArgsConstructor
    @Data
    public static class OptionTraitRequest {
        private Long optionTraitId;
        private String name;
        private Boolean isDefault; //productOptionTrait
        private double extraPrice; //productOptionTrait
        private int extraCalories; //productOptionTrait
    }
}
