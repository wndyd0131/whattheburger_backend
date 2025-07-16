package com.whattheburger.backend.service.dto.product;

import com.whattheburger.backend.controller.dto.product.QuantityDto;
import com.whattheburger.backend.domain.enums.CountType;
import com.whattheburger.backend.domain.enums.MeasureType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class OptionRequest {
    private Long optionId;
    private Boolean isDefault;
    private CountType countType;
    private MeasureType measureType;
    private Integer defaultQuantity;
    private Integer maxQuantity;
    private Double extraPrice;
    private Integer orderIndex;
    private List<QuantityDto> quantityDetails;
    private List<OptionTraitRequest> optionTraitRequests;
}