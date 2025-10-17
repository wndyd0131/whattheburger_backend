package com.whattheburger.backend.controller.dto.store;

import com.whattheburger.backend.domain.enums.ProductType;
import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class StoreProductReadResponseDto {
    private Long productId;
    private String name;
    private BigDecimal price;
    private String briefInfo;
    private String imageSource;
    private ProductType productType;
}
