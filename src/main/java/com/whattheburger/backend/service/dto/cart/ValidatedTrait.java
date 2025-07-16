package com.whattheburger.backend.service.dto.cart;

import com.whattheburger.backend.domain.ProductOptionTrait;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ValidatedTrait {
    private ProductOptionTrait productOptionTrait;
    private Integer currentValue;
}
