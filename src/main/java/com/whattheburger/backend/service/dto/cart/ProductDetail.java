package com.whattheburger.backend.service.dto.cart;

import com.whattheburger.backend.domain.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductDetail {
    private Long productId;
    private String productName;
    private ProductType productType;
    private Double productPrice;
    private String imageSource;
}
