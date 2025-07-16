package com.whattheburger.backend.controller.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class ProductOptionTraitRequest {
    private Long productOptionTraitId;
    private Integer value;
}