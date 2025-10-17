package com.whattheburger.backend.controller.dto.store;

import com.whattheburger.backend.domain.enums.ModifyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class StoreTraitModifyRequest {
    private Long traitId;
    private Integer defaultSelection;
    private BigDecimal extraPrice;
    private ModifyType modifyType;
}
