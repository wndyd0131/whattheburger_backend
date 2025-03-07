package com.whataburger.whataburgerproject.controller.dto;
import com.whataburger.whataburgerproject.domain.Product;
import com.whataburger.whataburgerproject.domain.enums.ProductType;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductCreateRequestDTO {
    private String productName;
    private double productPrice;
    private String briefInfo;
    private String imageSource;
    private Long categoryId;
    private ProductType productType;
    private List<OptionRequest> options;

    @NoArgsConstructor
    @Data
    public static class OptionRequest {
        private Long optionId;
        private Boolean isDefault;
        private int defaultQuantity;
        private int maxQuantity;
        private double extraPrice;
    }

    public Product toEntity() {
        return new Product(productName, productPrice, briefInfo, imageSource, productType);
    }
}