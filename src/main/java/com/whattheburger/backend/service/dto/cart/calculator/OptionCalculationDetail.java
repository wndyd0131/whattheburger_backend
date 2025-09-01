package com.whattheburger.backend.service.dto.cart.calculator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OptionCalculationDetail {
    private Long productOptionId;
    private BigDecimal calculatedOptionPrice; // option + trait
    private List<TraitCalculationDetail> traitCalculationDetails; // traits' individual price
    private QuantityCalculationDetail quantityCalculationDetail;

    public static OptionCalculationDetail unselected(Long productOptionId) {
        return new OptionCalculationDetail(
                productOptionId,
                BigDecimal.ZERO,
                Collections.emptyList(),
                null
        );
    }
}
