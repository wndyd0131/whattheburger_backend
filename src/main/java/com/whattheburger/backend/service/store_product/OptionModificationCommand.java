package com.whattheburger.backend.service.store_product;

import com.whattheburger.backend.domain.StoreProduct;
import com.whattheburger.backend.domain.enums.ModifyType;

import java.math.BigDecimal;

public record OptionModificationCommand(
        Long productId,
        Long optionId,
        StoreProduct storeProduct,
        Boolean isDefault,
        Integer defaultQuantity,
        Integer maxQuantity,
        BigDecimal extraPrice,
        Integer orderIndex,
        ModifyType modifyType
) implements StoreProductModificationCommand {}
