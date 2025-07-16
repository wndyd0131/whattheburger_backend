package com.whattheburger.backend.utils;

import com.whattheburger.backend.domain.Category;
import com.whattheburger.backend.domain.CategoryProduct;
import com.whattheburger.backend.domain.Product;
import com.whattheburger.backend.domain.enums.ProductType;

import java.util.ArrayList;

public class MockProductFactory {
    public static Product createMockProduct() {
        return Product
                .builder()
                .id(1L)
                .name("Whattheburger")
                .price(5.99D)
                .briefInfo("")
                .imageSource("")
                .calories(590D)
                .productType(ProductType.ONLY)
                .productOptions(new ArrayList<>())
                .categoryProducts(new ArrayList<>())
                .build();
    }
    public static CategoryProduct createMockCategoryProduct(Category mockCategory, Product mockProduct) {
        CategoryProduct categoryProduct = CategoryProduct
                .builder()
                .id(1L)
                .category(mockCategory)
                .product(mockProduct)
                .build();
        mockCategory.getCategoryProducts().add(categoryProduct);
        mockProduct.getCategoryProducts().add(categoryProduct);
        return categoryProduct;
    }
}
