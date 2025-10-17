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
public class CartRequestDto {
    private Long storeId;
    private Long productId;
    private List<CustomRuleRequest> customRuleRequests;
    private Integer quantity;
}
