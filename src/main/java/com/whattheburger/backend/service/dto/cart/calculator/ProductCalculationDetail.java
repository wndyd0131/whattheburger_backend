package com.whattheburger.backend.service.dto.cart.calculator;

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
public class ProductCalculationDetail {
    private Long storeProductId;
    private List<CustomRuleCalculationDetail> customRuleCalculationDetails;
    private Integer quantity;
    private BigDecimal calculatedTotalPrice;
    private BigDecimal calculatedExtraPrice;
}
