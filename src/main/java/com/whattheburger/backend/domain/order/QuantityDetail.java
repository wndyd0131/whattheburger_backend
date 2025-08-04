package com.whattheburger.backend.domain.order;

import com.whattheburger.backend.domain.enums.QuantityType;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuantityDetail {
    private Long productOptionOptionQuantityId;
    private QuantityType quantityType;
    private BigDecimal quantityExtraPrice;
    private Double quantityExtraCalories;
}
