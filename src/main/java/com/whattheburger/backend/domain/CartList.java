package com.whattheburger.backend.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CartList {
    private List<Cart> carts;

    public CartList(List<Cart> carts) {
        this.carts = carts;
    }
}
