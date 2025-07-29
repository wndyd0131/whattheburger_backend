package com.whattheburger.backend.controller.dto.cart;

import com.whattheburger.backend.domain.enums.CustomRuleType;
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
public class CustomRuleResponseDto {
    private Long customRuleId;
    private String customRuleName;
    private Integer orderIndex;
    private List<OptionResponseDto> optionResponses;
    private BigDecimal customRuleTotalPrice;
    private CustomRuleType customRuleType;
    private Integer minSelection;
    private Integer maxSelection;
}
