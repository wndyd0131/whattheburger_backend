package com.whattheburger.backend.service.dto.cart;


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
    private String customRuleName;
    private Integer orderIndex;
    private List<OptionDetail> optionDetails;
}
