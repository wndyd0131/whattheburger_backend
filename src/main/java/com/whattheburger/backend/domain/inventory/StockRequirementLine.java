package com.whattheburger.backend.domain.inventory;

import com.whattheburger.backend.domain.enums.CountType;

public record StockRequirementLine(
        int productQuantity,
        CountType countType,
        long productOptionId,
        Integer optionQuantity,
        Long pooqId
) {
}
