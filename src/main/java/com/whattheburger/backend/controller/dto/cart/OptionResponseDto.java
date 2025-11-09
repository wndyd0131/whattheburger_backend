package com.whattheburger.backend.controller.dto.cart;

import com.whattheburger.backend.domain.enums.CountType;
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
public class OptionResponseDto {
    private Long productOptionId;
    private String optionName;
    private Boolean isDefault;
    private Integer defaultQuantity;
    private Integer optionQuantity; // requested quantity
    private Integer maxQuantity;
    private BigDecimal basePrice;
    private Double baseCalories;
    private String imageSource;
    private Boolean isSelected;
    private CountType countType;
    private Integer orderIndex;
    private BigDecimal optionTotalPrice;
    private List<OptionTraitResponseDto> optionTraitResponses;
    private QuantityDetailResponse quantityDetailResponse;
}
