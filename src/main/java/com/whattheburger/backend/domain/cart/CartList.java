package com.whattheburger.backend.domain.cart;

import com.whattheburger.backend.domain.cart.Cart;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class CartList {
    private List<Cart> carts;
    private Long storeId;
    private UUID sessionId;

    public CartList(Long storeId, List<Cart> carts) {
        this.sessionId = UUID.randomUUID();
        this.carts = carts;
        this.storeId = storeId;
    }

    public void clearCartList() {
        this.carts.clear();
    }
}
