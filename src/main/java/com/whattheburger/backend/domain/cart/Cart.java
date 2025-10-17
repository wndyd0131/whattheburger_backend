package com.whattheburger.backend.domain.cart;

import com.whattheburger.backend.controller.dto.cart.CustomRuleRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class Cart {
    private Long storeId;
    private Long productId;
    private Integer quantity;
    private List<CustomRuleRequest> customRuleRequests = new ArrayList<>();

    public Cart(Long storeId, Long productId, Integer quantity, List<CustomRuleRequest> customRuleRequests) {
        this.storeId = storeId;
        this.productId = productId;
        this.quantity = quantity;
        this.customRuleRequests = customRuleRequests;
    }

    public void updateCustomRules(List<CustomRuleRequest> customRuleRequests) {
        this.customRuleRequests = customRuleRequests;
    }
}
