package com.whattheburger.backend.controller.dto.option;

import com.whattheburger.backend.domain.enums.QuantityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class OptionQuantityDto {
    private Long id;
    private QuantityType quantityType;
}
