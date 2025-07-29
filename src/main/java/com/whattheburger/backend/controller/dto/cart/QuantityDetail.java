package com.whattheburger.backend.controller.dto.cart;

import com.whattheburger.backend.domain.enums.QuantityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class QuantityDetail {
    private Long quantityId; // productOptionOptionQuantity
    private String labelCode;
    private BigDecimal extraPrice;
    private Double extraCalories;
    private Boolean isDefault; // ProductOptionOptionQuantity
    private QuantityType quantityType;
}
