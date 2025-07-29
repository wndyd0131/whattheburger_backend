package com.whattheburger.backend.controller.dto.cart;

import com.whattheburger.backend.domain.enums.OptionTraitType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OptionTraitResponseDto {
    private Long productOptionTraitId;
    private Integer currentValue;
    private String labelCode;
    private String optionTraitName;
    private OptionTraitType optionTraitType;
    private BigDecimal traitTotalPrice;
    private Integer defaultSelection;
    private BigDecimal basePrice;
    private Double baseCalories;
}
