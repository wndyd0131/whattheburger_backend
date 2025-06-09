package com.whattheburger.backend.controller.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Selection {
    private List<CustomRuleDetail> customRuleDetails;
    private Double totalCalories;
    private Double totalExtraPrice;
}
