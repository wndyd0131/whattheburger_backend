package com.whattheburger.backend.controller.dto;

import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductCreateResponseDTO {
    private Long id;
    private String name;
    private Double price;
    private String briefInfo;
    private String imageSource;
}
