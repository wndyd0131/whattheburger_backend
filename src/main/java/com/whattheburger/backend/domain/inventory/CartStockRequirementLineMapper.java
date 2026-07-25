package com.whattheburger.backend.domain.inventory;

import com.whattheburger.backend.controller.dto.cart.CustomRuleRequest;
import com.whattheburger.backend.controller.dto.cart.OptionRequest;
import com.whattheburger.backend.controller.dto.cart.QuantityDetailRequest;
import com.whattheburger.backend.domain.ProductOption;
import com.whattheburger.backend.domain.cart.Cart;
import com.whattheburger.backend.domain.cart.CartList;
import com.whattheburger.backend.domain.enums.CountType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CartStockRequirementLineMapper {

    private CartStockRequirementLineMapper() {
    }

    public static List<StockRequirementLine> fromCart(Cart cart, Map<Long, ProductOption> productOptionMap) {
        List<StockRequirementLine> lines = new ArrayList<>();
        int productQuantity = cart.getQuantity();

        for (CustomRuleRequest customRuleRequest : cart.getCustomRuleRequests()) {
            for (OptionRequest optionRequest : customRuleRequest.getOptionRequests()) {
                if (!Boolean.TRUE.equals(optionRequest.getIsSelected())) {
                    continue;
                }

                ProductOption productOption = productOptionMap.get(optionRequest.getProductOptionId());
                if (productOption == null) {
                    continue;
                }

                CountType countType = productOption.getCountType();
                if (countType == null || countType == CountType.NONE) {
                    continue;
                }

                Long pooqId = null;
                if (countType == CountType.UNCOUNTABLE) {
                    QuantityDetailRequest quantityDetailRequest = optionRequest.getQuantityDetailRequest();
                    pooqId = quantityDetailRequest != null ? quantityDetailRequest.getId() : null;
                }

                lines.add(new StockRequirementLine(
                        productQuantity,
                        countType,
                        optionRequest.getProductOptionId(),
                        optionRequest.getOptionQuantity(),
                        pooqId
                ));
            }
        }

        return lines;
    }

    public static List<StockRequirementLine> fromCartList(CartList cartList, Map<Long, ProductOption> productOptionMap) {
        List<StockRequirementLine> lines = new ArrayList<>();
        for (Cart cart : cartList.getCarts()) {
            lines.addAll(fromCart(cart, productOptionMap));
        }
        return lines;
    }

    public static Set<Long> collectCountableProductOptionIds(CartList cartList, Map<Long, ProductOption> productOptionMap) {
        Set<Long> ids = new HashSet<>();
        for (Cart cart : cartList.getCarts()) {
            for (CustomRuleRequest customRuleRequest : cart.getCustomRuleRequests()) {
                for (OptionRequest optionRequest : customRuleRequest.getOptionRequests()) {
                    if (!Boolean.TRUE.equals(optionRequest.getIsSelected())) {
                        continue;
                    }
                    ProductOption productOption = productOptionMap.get(optionRequest.getProductOptionId());
                    if (productOption != null && productOption.getCountType() == CountType.COUNTABLE) {
                        ids.add(optionRequest.getProductOptionId());
                    }
                }
            }
        }
        return ids;
    }

    public static Set<Long> collectUncountablePooqIds(CartList cartList, Map<Long, ProductOption> productOptionMap) {
        Set<Long> ids = new HashSet<>();
        for (Cart cart : cartList.getCarts()) {
            for (CustomRuleRequest customRuleRequest : cart.getCustomRuleRequests()) {
                for (OptionRequest optionRequest : customRuleRequest.getOptionRequests()) {
                    if (!Boolean.TRUE.equals(optionRequest.getIsSelected())) {
                        continue;
                    }
                    ProductOption productOption = productOptionMap.get(optionRequest.getProductOptionId());
                    if (productOption != null && productOption.getCountType() == CountType.UNCOUNTABLE) {
                        QuantityDetailRequest quantityDetailRequest = optionRequest.getQuantityDetailRequest();
                        if (quantityDetailRequest != null) {
                            ids.add(quantityDetailRequest.getId());
                        }
                    }
                }
            }
        }
        return ids;
    }
}
