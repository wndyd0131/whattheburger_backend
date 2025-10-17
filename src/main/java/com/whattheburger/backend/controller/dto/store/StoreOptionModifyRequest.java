package com.whattheburger.backend.controller.dto.store;

import com.whattheburger.backend.controller.dto.product.QuantityDto;
import com.whattheburger.backend.domain.enums.CountType;
import com.whattheburger.backend.domain.enums.MeasureType;
import com.whattheburger.backend.domain.enums.ModifyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class StoreOptionModifyRequest {
    private Long optionId;
    private Boolean isDefault;
    private Integer defaultQuantity;
    private Integer maxQuantity;
    private BigDecimal extraPrice;
    private Integer orderIndex;
    private ModifyType modifyType;
    private List<StoreQuantityModifyRequest> quantityRequest;
    private List<StoreTraitModifyRequest> optionTraitRequests;
}