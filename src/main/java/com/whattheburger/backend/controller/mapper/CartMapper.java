package com.whattheburger.backend.controller.mapper;

import com.whattheburger.backend.controller.dto.cart.*;
import com.whattheburger.backend.domain.cart.Cart;
import com.whattheburger.backend.domain.Product;

import java.util.ArrayList;
import java.util.List;

public class CartMapper {
    public CartResponseDto toResponse(Cart cart, Product product) {
        List<CustomRuleResponseDto> customRuleResponses = new ArrayList<>();
        for (CustomRuleRequest customRuleRequest : cart.getCustomRuleRequests()) {
            Long customRuleId = customRuleRequest.getCustomRuleId();
        }

        return new CartResponseDto(
        );
    }
}
