package com.whattheburger.backend.service.dto.cart;

import com.whattheburger.backend.domain.CustomRule;
import com.whattheburger.backend.service.dto.cart.calculator.ProcessedOptionDto;
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
public class ProcessedCustomRuleDto {
    private CustomRule customRule;
    private List<ProcessedOptionDto> processedOptionDtos;
    private BigDecimal calculatedCustomRulePrice;
}
