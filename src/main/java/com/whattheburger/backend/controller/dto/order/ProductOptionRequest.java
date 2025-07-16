package com.whattheburger.backend.controller.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class ProductOptionRequest {
    private Long productOptionId;
    private Integer quantity;
    private List<ProductOptionTraitRequest> productOptionTraitRequests;
}