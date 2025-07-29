package com.whattheburger.backend.service.dto.cart.calculator;

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
public class CustomRuleCalcDetail {
    private Long customRuleId;
    private List<OptionCalcDetail> optionCalcDetails;
    private BigDecimal optionTotalPrice;
}
