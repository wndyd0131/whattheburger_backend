package com.whattheburger.backend.service.dto.cart.calculator;

import com.whattheburger.backend.service.dto.cart.OptionDetail;
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
public class CalculatedCustomRuleDto {
    private Long customRuleId;
    private String customRuleName;
    private Integer orderIndex;
    private List<OptionDetail> optionDetails;
    private List<CalculatedOptionDto> calculatedOptionDtos;
    private BigDecimal totalPrice;
}
