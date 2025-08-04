package com.whattheburger.backend.controller.dto.order;

import com.whattheburger.backend.domain.enums.CountType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class OptionResponseDto {
    private Long id;
    private Long productOptionId;
    private String name;
    private CountType countType;
    private BigDecimal calculatedPrice;
    private Double calculatedCalories;
    private Integer quantity;
    private QuantityDetail quantityDetail;
    private List<TraitResponseDto> traitResponseDtos;
}
