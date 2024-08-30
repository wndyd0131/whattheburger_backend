package com.whataburger.whataburgerproject.controller.dto;

import com.whataburger.whataburgerproject.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductCreateRequestDTO {
    private String name;
    private int price;
    private String ingredientInfo;

    public Product toEntity() {
        return new Product(name, price, ingredientInfo);
    }
}
