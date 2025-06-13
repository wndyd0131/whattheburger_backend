package com.whattheburger.backend.controller.mapper;

import com.whattheburger.backend.controller.dto.cart.*;
import com.whattheburger.backend.domain.Cart;
import com.whattheburger.backend.domain.Product;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

public class CartMapper {
    public CartResponseDto toResponse(Cart cart, Product product) {
        List<CustomRuleResponse> customRuleResponses = new ArrayList<>();
        for (CustomRuleRequest customRuleRequest : cart.getCustomRuleRequests()) {
            Long customRuleId = customRuleRequest.getCustomRuleId();
        }

        return new CartResponseDto(
        );
    }
}
