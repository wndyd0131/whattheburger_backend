package com.whattheburger.backend.service.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class OptionTraitRequest {
    private Long optionTraitId;
    private Integer defaultSelection;
    private BigDecimal extraPrice;
    private Double extraCalories;
}