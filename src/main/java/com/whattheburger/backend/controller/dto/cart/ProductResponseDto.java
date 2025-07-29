package com.whattheburger.backend.controller.dto.cart;

import com.whattheburger.backend.domain.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductResponseDto {
    private Long productId;
    private String productName;
    private ProductType productType;
    private BigDecimal basePrice;
    private String imageSource;
    private String briefInfo;
    private Double calories;
    private List<CustomRuleResponseDto> customRuleResponses;
    private Integer quantity;
    private BigDecimal productTotalPrice;
    private BigDecimal productExtraPrice;
}
