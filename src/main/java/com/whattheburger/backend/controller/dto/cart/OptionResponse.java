package com.whattheburger.backend.controller.dto.cart;

import com.whattheburger.backend.domain.enums.CountType;
import com.whattheburger.backend.domain.enums.MeasureType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OptionResponse {
    private Long productOptionId;
    private Integer optionQuantity;
    private Boolean isSelected;
    private List<OptionTraitResponse> optionTraitResponses;
    private CountType countType;
    private MeasureType measureType;
    private String optionName;
    private Integer orderIndex;
    private QuantityDetailResponse quantityDetailResponse;
}
