package com.whattheburger.backend.domain;

import com.whattheburger.backend.controller.dto.cart.CustomRuleDetail;
import com.whattheburger.backend.controller.dto.cart.SelectionDetail;
import com.whattheburger.backend.controller.dto.cart.ProductDetail;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class Cart {
    private Long productId;
    private List<CustomRuleDetail> customRuleDetails = new ArrayList<>();

    public Cart(Long productId, List<CustomRuleDetail> customRuleDetails) {
        this.productId = productId;
        this.customRuleDetails = customRuleDetails;
    }
}
