package com.whattheburger.backend.service.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class OptionTraitRequest {
    private Long optionTraitId;
    private Integer defaultSelection;
    private Double extraPrice;
    private Double extraCalories;
}