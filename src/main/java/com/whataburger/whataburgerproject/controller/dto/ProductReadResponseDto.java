package com.whataburger.whataburgerproject.controller.dto;

import com.whataburger.whataburgerproject.domain.enums.ProductType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
