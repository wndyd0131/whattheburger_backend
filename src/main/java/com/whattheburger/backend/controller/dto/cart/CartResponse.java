package com.whattheburger.backend.controller.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CartResponse {
    private ProductResponse productResponse;
    private List<CustomRuleResponse> customRuleResponses;
    private Integer quantity;
}
