package com.whataburger.whataburgerproject.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductCreateDetailsDto {
    private String name;
    private Double price;
    private String briefInfo;
    private String imageSource;
}
