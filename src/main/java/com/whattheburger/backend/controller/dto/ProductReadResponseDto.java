package com.whattheburger.backend.controller.dto;

import com.whattheburger.backend.domain.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductReadResponseDto {
    private Long productId;
    private String name;
    private Double price;
    private String briefInfo;
    private String imageSource;
    private ProductType productType;
}
