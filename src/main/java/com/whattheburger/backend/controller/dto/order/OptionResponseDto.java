package com.whattheburger.backend.controller.dto.order;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private BigDecimal basePrice;
    private Double calculatedCalories;
    private Integer quantity;
    private QuantityDetail quantityDetail;
    @JsonProperty("traitResponses")
    private List<TraitResponseDto> traitResponseDtos;
}
