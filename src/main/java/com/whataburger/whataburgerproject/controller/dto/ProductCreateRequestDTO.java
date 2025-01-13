package com.whataburger.whataburgerproject.controller.dto;
import com.whataburger.whataburgerproject.domain.Product;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductCreateRequestDTO {
    private String productName;
    private double productPrice;
    private String ingredientInfo;
    private String imageSource;
    private List<OptionRequest> options;

    @NoArgsConstructor
    @Data
    public static class OptionRequest {
        private Long optionId;
        private Boolean isDefault;
        private int defaultQuantity;
    }

    public Product toEntity() {
        return new Product(productName, productPrice, ingredientInfo, imageSource);
    }
}
