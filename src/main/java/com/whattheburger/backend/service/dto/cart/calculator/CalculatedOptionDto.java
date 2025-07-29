package com.whattheburger.backend.service.dto.cart.calculator;

import com.whattheburger.backend.domain.enums.CountType;
import com.whattheburger.backend.domain.enums.MeasureType;
import com.whattheburger.backend.service.dto.cart.OptionTraitDetail;
import com.whattheburger.backend.service.dto.cart.QuantityDetail;
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
public class CalculatedOptionDto {
    private Long productOptionId;
    private Integer quantity;
    private Boolean isSelected;
    private List<OptionTraitDetail> optionTraitDetails;
    private CountType countType;
    private MeasureType measureType;
    private String optionName;
    private Integer orderIndex;
    private QuantityDetail quantityDetail;
    private List<CalculatedTraitDto> calculatedTraitDtos;
    private BigDecimal totalPrice;
}
