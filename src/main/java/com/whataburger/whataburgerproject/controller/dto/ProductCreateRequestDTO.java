package com.whataburger.whataburgerproject.controller.dto;

import com.whataburger.whataburgerproject.domain.Product;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductCreateRequestDTO {
    private String name;
    private double price;
    private String ingredientInfo;

    public Product toEntity() {
        return new Product(name, price, ingredientInfo);
    }
}
