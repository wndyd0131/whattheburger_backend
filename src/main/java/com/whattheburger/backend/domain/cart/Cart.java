package com.whattheburger.backend.domain.cart;

import com.whattheburger.backend.controller.dto.cart.CartOptionModifyRequestDto;
import com.whattheburger.backend.controller.dto.cart.CartProductModifyRequestDto;
import com.whattheburger.backend.controller.dto.cart.CustomRuleRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class Cart {
    private Long storeProductId;
    private Integer quantity;
    private List<CustomRuleRequest> customRuleRequests = new ArrayList<>();

    public Cart(Long storeProductId, Integer quantity, List<CustomRuleRequest> customRuleRequests) {
        this.storeProductId = storeProductId;
        this.quantity = quantity;
        this.customRuleRequests = customRuleRequests;
    }

    public void updateCustomRules(List<CustomRuleRequest> customRuleRequests) {
        this.customRuleRequests = customRuleRequests;
    }

    public void updateProduct(int quantity) {
        this.quantity = quantity;
    }
}
