package com.whattheburger.backend.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductReadByCategoryIdResponseDto {
    private Long productId;
    private String productName;
    private Double productPrice;
    private String imageSource;
    private String briefInfo;
}
