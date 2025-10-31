package com.whattheburger.backend.service.dto.product;

import com.whattheburger.backend.domain.Product;
import com.whattheburger.backend.domain.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Data
public class ProductCreateDto {
    private String productName;
    private BigDecimal productPrice;
    private Double productCalories;
    private ProductType productType;
    private String briefInfo;
    private String imageSource;
    private List<Long> categoryIds;
    private List<CustomRuleRequest> customRuleRequests;


    public Product toEntity() {
        return new Product(productName, productPrice, briefInfo, productCalories, productType);
    }
}
