package com.whattheburger.backend.controller.dto.cart;

import com.whattheburger.backend.domain.enums.CustomRuleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CustomRuleResponse {
    private Long customRuleId;
    private String customRuleName;
    private Integer orderIndex;
//    private Integer selectedCount;
    private List<OptionResponse> optionResponses;
}
