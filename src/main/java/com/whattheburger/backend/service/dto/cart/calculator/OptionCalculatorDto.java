package com.whattheburger.backend.service.dto.cart.calculator;

import com.whattheburger.backend.domain.enums.CountType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OptionCalculatorDto {
    private Long productOptionId;
    private BigDecimal price;
    private Integer quantity;
    private Boolean isSelected;
    private Boolean isDefault;
    private Integer defaultQuantity;
    private CountType countType;
    private QuantityCalculatorDto quantityCalculatorDto;
    @NotNull(message = "TraitResult cannot be null")
    private TraitCalculationResult traitCalculationResult;
}
