package com.whattheburger.backend.domain;

import com.whattheburger.backend.controller.dto.cart.CustomRuleRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class Cart {
    private Long productId;
    private Integer quantity;
    private List<CustomRuleRequest> customRuleRequests = new ArrayList<>();

    public Cart(Long productId, Integer quantity, List<CustomRuleRequest> customRuleRequests) {
        this.productId = productId;
        this.quantity = quantity;
        this.customRuleRequests = customRuleRequests;
    }
}
