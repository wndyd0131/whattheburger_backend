package com.whattheburger.backend.controller.mapper;

import com.whattheburger.backend.controller.dto.cart.CartResponseDto;
import com.whattheburger.backend.domain.Cart;
import org.springframework.stereotype.Component;

@Component
public class CartMapper {
    public CartResponseDto toResponse(Cart cart) {
        return new CartResponseDto(
                cart.getProductId(),
                cart.getCustomRuleDetails()
        );
    }
}
