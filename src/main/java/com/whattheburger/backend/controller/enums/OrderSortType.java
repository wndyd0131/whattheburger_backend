package com.whattheburger.backend.controller.enums;

import lombok.Getter;
import org.springframework.data.domain.Sort;

import java.util.function.Supplier;

@Getter
public enum OrderSortType {

    CREATED_AT_DESC(Sort.by("createdAt").descending()),
    CREATED_AT_ASC(Sort.by("createdAt").ascending()),
    TOTAL_PRICE_DESC(Sort.by("totalPrice").descending());

    private final Sort sort;

    OrderSortType(Sort sort) {
        this.sort = sort;
    }
}

