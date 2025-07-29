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
public class ProductCalcDetail {
    private Long productId;
    private BigDecimal basePrice;
    private Integer quantity;
    private List<CustomRuleCalcDetail> customRuleCalcDetails;
    private BigDecimal customRuleTotalPrice;
    private BigDecimal calculatedProductPrice;

    public void changeCalculatedProductPrice(BigDecimal price) {
        this.calculatedProductPrice = price;
    }
}
