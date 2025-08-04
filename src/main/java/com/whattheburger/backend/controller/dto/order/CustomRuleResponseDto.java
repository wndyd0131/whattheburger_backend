package com.whattheburger.backend.controller.dto.order;

import jakarta.persistence.Column;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CustomRuleResponseDto {
    private Long id;
    private Long customRuleId;
    private BigDecimal calculatedPrice;
    private String name;
    private List<OptionResponseDto> optionResponseDtos;
}
