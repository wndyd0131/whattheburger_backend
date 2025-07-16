package com.whattheburger.backend.service.dto.cart;

import com.whattheburger.backend.domain.enums.QuantityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class QuantityDetail {
    private Long id; // productOptionOptionQuantity
    private QuantityType quantityType;
}
