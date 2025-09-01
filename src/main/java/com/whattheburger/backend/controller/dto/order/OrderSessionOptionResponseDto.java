package com.whattheburger.backend.controller.dto.order;

import com.whattheburger.backend.domain.enums.CountType;
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
public class OrderSessionOptionResponseDto {
    private Long id;
    private Long productOptionId;
    private String name;
    private CountType countType;
    private BigDecimal calculatedPrice;
    private BigDecimal basePrice;
    private Double calculatedCalories;
    private Integer quantity;
    private QuantityDetail quantityDetail;
    private List<OrderSessionTraitResponseDto> traitResponses;
}
