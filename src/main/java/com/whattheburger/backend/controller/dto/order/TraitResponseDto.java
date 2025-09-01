package com.whattheburger.backend.controller.dto.order;

import com.whattheburger.backend.domain.enums.OptionTraitType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class TraitResponseDto {
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
