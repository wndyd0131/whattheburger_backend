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
public class ProductCreateDetailsDto {
    private String name;
    private BigDecimal price;
    private String briefInfo;
    private String imageSource;
}
