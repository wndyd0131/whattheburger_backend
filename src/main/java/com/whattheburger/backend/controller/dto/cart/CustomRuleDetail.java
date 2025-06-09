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
public class CustomRuleDetail {
    private Long customRuleId;
    private List<OptionDetail> optionDetails;

//    private String customRuleName;
//    private CustomRuleType customRuleType;
//    private Integer maxSelection;
//    private Integer minSelection;
//    private Integer orderIndex;
//    private Integer selectedCount;
}
