package com.whataburger.whataburgerproject.controller.dto;
import com.whataburger.whataburgerproject.domain.Product;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductCreateRequestDTO {
    private String name;
    private double price;
    private String ingredientInfo;
    private String imageSource;
    private List<IngredientRequest> ingredients;

    @NoArgsConstructor
    @Data
    public static class IngredientRequest {
        private Long ingredientId;
    }

    public Product toEntity() {
        return new Product(name, price, ingredientInfo, imageSource);
    }
}
