package com.whattheburger.backend.controller.dto.product;

import com.whattheburger.backend.domain.enums.QuantityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class QuantityDto {
    private Long id;
    private QuantityType quantityType;
    private Boolean isDefault;
    private Double extraPrice;
}
