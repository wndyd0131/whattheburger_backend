package com.whattheburger.backend.utils;

import com.whattheburger.backend.controller.dto.store.StoreCustomRuleModifyRequest;
import com.whattheburger.backend.controller.dto.store.StoreOptionModifyRequest;
import com.whattheburger.backend.controller.dto.store.StoreProductModifyRequestDto;
import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.domain.enums.ModifyType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MockStoreProductFactory {
    public static StoreProduct createStoreProduct(Store store, Product product) {
        return new StoreProduct(store, product);
    }
    public static StoreProduct createStoreProduct(
            Long storeProductId,
            BigDecimal overridePrice,
            Boolean isActive,
            Store store,
            Product product
    ) {
        StoreProduct storeProduct = new StoreProduct(
                storeProductId,
                overridePrice,
                isActive,
                store,
                product,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );
        return storeProduct;
    }


    public static StoreProductModifyRequestDto createStoreProductEditRequestDto() {
        StoreOptionModifyRequest optionModifyRequest = StoreOptionModifyRequest
                .builder()
                .defaultQuantity(1)
                .extraPrice(BigDecimal.ZERO)
                .isDefault(true)
                .modifyType(ModifyType.OVERRIDE)
                .maxQuantity(4)
                .optionId(1L)
                .orderIndex(0)
                .quantityRequest(null)
                .optionTraitRequests(new ArrayList<>())
                .build();
        StoreCustomRuleModifyRequest customRuleModifyRequest = StoreCustomRuleModifyRequest
                .builder()
                .customRuleId(1L)
                .optionRequests(List.of(optionModifyRequest))
                .build();
        return StoreProductModifyRequestDto
                .builder()
                .productPrice(BigDecimal.valueOf(7.99))
                .customRuleRequests(List.of(customRuleModifyRequest))
                .build();
    }
}
