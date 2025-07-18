package com.whattheburger.backend.controller.dto.cart;

import com.whattheburger.backend.domain.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductDetail {
    private Long productId;
    private String productName;
    private ProductType productType;
    private BigDecimal productPrice;
    private String imageSource;
}
