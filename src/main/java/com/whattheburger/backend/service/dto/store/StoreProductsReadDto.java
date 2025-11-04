package com.whattheburger.backend.service.dto.store;

import com.whattheburger.backend.domain.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class StoreProductsReadDto {
    private Long productId;
    private String name;
    private BigDecimal price;
    private Double calories;
    private String briefInfo;
    private String imageSource;
    private ProductType productType;
    private List<CategoryDto> categories;

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryDto {
        private Long categoryId;
        private int orderIndex;
    }
}
