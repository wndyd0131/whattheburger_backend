package com.whattheburger.backend.service.dto;

import com.whattheburger.backend.domain.enums.ModifyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptionModificationDto {
    private Long optionId;
    private Boolean isDefault;
    private Integer defaultQuantity;
    private Integer maxQuantity;
    private BigDecimal extraPrice;
    private Integer orderIndex;
    private ModifyType modifyType;
}
