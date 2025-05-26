package com.whattheburger.backend.service.dto;

import com.whattheburger.backend.domain.Option;
import com.whattheburger.backend.domain.ProductOptionTrait;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProductOptionDto {
    private Option option;
    private ProductOptionTrait productOptionTrait;
}
