package com.whattheburger.backend.controller.dto;

import com.whattheburger.backend.domain.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategorizedStoreProductsReadDto {
    private Long categoryId;
    private String categoryName;
    private Integer orderIndex;
    private List<StoreProductDto> products;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class StoreProductDto {
        private Long storeProductId;
        private String name;
        private BigDecimal price;
        private String briefInfo;
        private String imageSource;
        private ProductType productType;
    }
}
