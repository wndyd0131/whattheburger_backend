package com.whattheburger.backend.utils;

import com.whattheburger.backend.controller.dto.cart.CustomRuleRequest;
import com.whattheburger.backend.controller.dto.cart.OptionRequest;
import com.whattheburger.backend.controller.dto.cart.OptionTraitRequest;
import com.whattheburger.backend.controller.dto.cart.QuantityDetailRequest;
import com.whattheburger.backend.domain.cart.Cart;
import com.whattheburger.backend.domain.cart.CartList;

import java.util.List;

public class MockCartFactory {
    public static CartList createMockCartList() {
        return getCartList(
                1L,
                1,
                1L,
                1L,
                1,
                true,
                1L,
                1
        );
    }

    public static CartList createNonExistingProductIdCartList() {
        return getCartList(
                2L,
                1,
                1L,
                1L,
                1,
                true,
                1L,
                1
        );
    }

    private static CartList getCartList(
            Long productId,
            Integer cartQuantity,
            Long customRuleId,
            Long productOptionId,
            Integer optionQuantity,
            Boolean isOptionSelected,
            Long productOptionTraitId,
            Integer productOptionTraitValue
    ) {
        List<OptionTraitRequest> optionTraitRequests = List.of(
                new OptionTraitRequest(
                        productOptionTraitId,
                        productOptionTraitValue
                )
        );
//        QuantityDetailRequest quantityDetailRequest = new QuantityDetailRequest(
//                1L
//        );
        List<OptionRequest> optionRequests = List.of(
                new OptionRequest(
                        productOptionId,
                        optionQuantity,
                        isOptionSelected,
                        optionTraitRequests,
                        null
                )
        );
        List<CustomRuleRequest> customRuleRequests = List.of(
                new CustomRuleRequest(
                        customRuleId,
                        optionRequests
                )
        );
        List<Cart> carts = List.of(
                new Cart(
                        productId,
                        cartQuantity,
                        customRuleRequests
                )
        );
        return new CartList(carts);
    }
}
