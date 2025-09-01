package com.whattheburger.backend.domain.order;

import com.whattheburger.backend.domain.enums.OptionTraitType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class OrderSessionOptionTrait {
    private Long productOptionTraitId;
    private String name;
    private String labelCode;
    private BigDecimal totalPrice;
    private BigDecimal basePrice;
    private Double calculatedCalories;
    private OptionTraitType optionTraitType;
    private Integer selectedValue;
}
