package com.whattheburger.backend.service.dto.cart.calculator;

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
public class CalculatedTraitDto {
    private BigDecimal price;
    private Integer defaultSelection;
    private Integer requestedSelection;
    private OptionTraitType optionTraitType;
}
