package com.whattheburger.backend.controller.dto;

import lombok.*;

import java.math.BigDecimal;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductCreateResponseDTO {
    private Long id;
    private String name;
    private BigDecimal price;
    private String briefInfo;
    private String imageSource;
}
