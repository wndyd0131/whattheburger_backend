package com.whattheburger.backend.domain.order;

import com.whattheburger.backend.domain.enums.QuantityType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    @Enumerated(EnumType.STRING)
    private QuantityType quantityType;
    private BigDecimal quantityExtraPrice;
    private Double quantityExtraCalories;
}
