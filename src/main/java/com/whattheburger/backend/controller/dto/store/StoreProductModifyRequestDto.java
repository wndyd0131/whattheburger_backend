package com.whattheburger.backend.controller.dto.store;

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
public class StoreProductModifyRequestDto {
    private BigDecimal productPrice;
    private List<StoreCustomRuleModifyRequest> customRuleRequests;
}
