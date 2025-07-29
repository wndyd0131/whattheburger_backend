package com.whattheburger.backend.service.dto.cart.calculator;

import com.whattheburger.backend.domain.ProductOption;
import com.whattheburger.backend.service.dto.cart.ProcessedQuantityDto;
import com.whattheburger.backend.service.dto.cart.ProcessedTraitDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProcessedOptionDto {
    private ProductOption productOption;
    private Integer quantity;
    private Boolean isSelected;
    private ProcessedQuantityDto processedQuantityDto;
    private List<ProcessedTraitDto> processedTraitDtos;
    private BigDecimal calculatedOptionPrice;
}
