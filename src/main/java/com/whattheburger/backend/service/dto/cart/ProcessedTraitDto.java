package com.whattheburger.backend.service.dto.cart;

import com.whattheburger.backend.domain.ProductOptionTrait;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProcessedTraitDto {
    private ProductOptionTrait productOptionTrait;
    private Integer currentValue;
    private BigDecimal calculatedTraitPrice;
}
