package com.whattheburger.backend.service.dto.cart.calculator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartCalculationResult implements PriceCalculationResult {
    private List<ProductCalculationDetail> productCalculationDetails;
    private BigDecimal cartTotalPrice;
}
