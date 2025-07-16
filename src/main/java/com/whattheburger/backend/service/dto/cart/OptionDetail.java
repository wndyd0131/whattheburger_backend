package com.whattheburger.backend.service.dto.cart;

import com.whattheburger.backend.controller.dto.cart.OptionTraitResponse;
import com.whattheburger.backend.controller.dto.cart.QuantityDetailResponse;
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
public class OptionDetail {
    private Long productOptionId;
    private Integer quantity;
    private Boolean isSelected;
    private List<OptionTraitDetail> optionTraitDetails;
    private CountType countType;
    private MeasureType measureType;
    private String optionName;
    private Integer orderIndex;
    private QuantityDetail quantityDetail;
}
