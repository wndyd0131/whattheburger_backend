package com.whattheburger.backend.service.dto.cart.calculator;

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
public class CalculatedProductDto {
    private Long productId;
    private String productName;
    private ProductType productType;
    private BigDecimal productPrice;
    private String imageSource;
    private List<CalculatedCustomRuleDto> calculatedCustomRuleDtos;
    private BigDecimal totalPrice;
}
