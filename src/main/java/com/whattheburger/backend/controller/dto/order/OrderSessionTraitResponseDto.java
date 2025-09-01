package com.whattheburger.backend.controller.dto.order;

import com.whattheburger.backend.domain.enums.OptionTraitType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class OrderSessionTraitResponseDto {
    private Long id;
    private Long productOptionTraitId;
    private String name;
    private String labelCode;
    private BigDecimal calculatedPrice;
    private BigDecimal basePrice;
    private Double calculatedCalories;
    private OptionTraitType optionTraitType;
    private Integer selectedValue;
}
