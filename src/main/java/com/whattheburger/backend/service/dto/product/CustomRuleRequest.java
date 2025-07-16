package com.whattheburger.backend.service.dto.product;

import com.whattheburger.backend.domain.enums.CustomRuleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class CustomRuleRequest {
    private String customRuleName;
    private CustomRuleType customRuleType;
    private Integer orderIndex;
    private Integer minSelection;
    private Integer maxSelection;
    private List<OptionRequest> optionRequests;
}