package com.whattheburger.backend.controller.dto.cart;

import com.whattheburger.backend.domain.enums.QuantityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class QuantityDetailResponse {
    private Long id; // productOptionOptionQuantity
    private QuantityType quantityType;
}
