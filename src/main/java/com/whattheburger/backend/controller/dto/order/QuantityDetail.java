package com.whattheburger.backend.controller.dto.order;

import com.whattheburger.backend.domain.enums.QuantityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class QuantityDetail {
    private Long productOptionOptionQuantityId;
    private QuantityType quantityType;
    private BigDecimal extraPrice;
    private Double extraCalories;
}
