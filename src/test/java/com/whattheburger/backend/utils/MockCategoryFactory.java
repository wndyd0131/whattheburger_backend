package com.whattheburger.backend.utils;

import com.whattheburger.backend.domain.Category;

import java.util.ArrayList;

public class MockCategoryFactory {
    public static Category createMockCategory() {
        return Category
                .builder()
                .id(1L)
                .name("Burgers")
                .categoryProducts(new ArrayList<>())
                .build();
    }
}
