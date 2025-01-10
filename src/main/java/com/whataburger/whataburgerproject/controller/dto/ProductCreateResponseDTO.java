package com.whataburger.whataburgerproject.controller.dto;

import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductCreateResponseDTO {
    private Long id;
    private String name;
    private double price;
    private String ingredientInfo;
    private String imageSource;
}
