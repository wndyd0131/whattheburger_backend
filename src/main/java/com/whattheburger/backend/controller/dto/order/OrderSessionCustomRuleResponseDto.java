package com.whattheburger.backend.controller.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class OrderSessionCustomRuleResponseDto {
    private Long id;
    private Long customRuleId;
    private BigDecimal calculatedPrice;
    private String name;
    private List<OrderSessionOptionResponseDto> optionResponses;
}
