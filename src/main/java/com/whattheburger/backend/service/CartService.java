package com.whattheburger.backend.service;

import com.whattheburger.backend.controller.dto.cart.CartRequestDto;
import com.whattheburger.backend.domain.Cart;
import com.whattheburger.backend.domain.CartList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final RedisTemplate<String, CartList> rt;

    public void saveCart(String sessionId, CartRequestDto cartRequestDto) {
        Cart cart = new Cart(cartRequestDto.getProductId(), cartRequestDto.getCustomRuleDetails());
        CartList cartList = Optional.ofNullable(rt.opsForValue().get("cart:" + sessionId)).orElse(new CartList(new ArrayList<>()));
        log.info("CartList {}", cartList);
        cartList.getCarts().add(cart);
        rt.opsForValue().set("cart:"+sessionId, cartList);
    }

    public CartList loadCart(String sessionId) {
        // nullPointerException
        CartList cartList = Optional.ofNullable(rt.opsForValue().get("cart:" + sessionId)).orElse(new CartList(new ArrayList<>()));
        return cartList;
    }
}
