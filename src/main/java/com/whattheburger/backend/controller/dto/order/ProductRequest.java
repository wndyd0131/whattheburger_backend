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
public class ProductRequest {
    private Long productId;
    private Integer quantity;
    private String forWhom;
    private List<ProductOptionRequest> productOptionRequests;
}