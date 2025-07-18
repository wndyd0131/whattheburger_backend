package com.whattheburger.backend.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductReadByCategoryIdResponseDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private Double calories;
    private String imageSource;
    private String briefInfo;
}
