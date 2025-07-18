package com.whattheburger.backend.controller.dto.cart;

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
public class Selection {
    private List<CustomRuleRequest> customRuleRequests;
    private Double totalCalories;
    private BigDecimal totalExtraPrice;
}
