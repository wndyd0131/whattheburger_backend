package com.whattheburger.backend.controller.dto.store;

import com.whattheburger.backend.domain.enums.CustomRuleType;
import com.whattheburger.backend.domain.enums.ModifyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class StoreCustomRuleModifyRequest {
    private Long customRuleId;
    private ModifyType modifyType;
    private List<StoreOptionModifyRequest> optionRequests;
}
