package com.whattheburger.backend.utils;

import com.whattheburger.backend.domain.Category;
import com.whattheburger.backend.domain.CategoryProduct;
import com.whattheburger.backend.domain.Product;
import com.whattheburger.backend.domain.ProductOption;
import com.whattheburger.backend.domain.enums.ProductType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MockProductFactory {

    public static Product createMockProduct() {
        return Product
                .builder()
                .id(1L)
                .name("Whattheburger")
                .price(new BigDecimal(5.99))
                .briefInfo("")
                .imageSource("")
                .calories(590D)
                .productType(ProductType.ONLY)
                .productOptions(new ArrayList<>())
                .categoryProducts(new ArrayList<>())
                .build();
    }

    public static Product createMockProduct(
            Long productId,
            String productName,
            BigDecimal price,
            String briefInfo,
            String imageSource,
            Double calories,
            ProductType productType
    ) {
        return Product
                .builder()
                .id(productId)
                .name(productName)
                .price(price)
                .briefInfo(briefInfo)
                .imageSource(imageSource)
                .calories(calories)
                .productType(productType)
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

    public static CategoryProduct createMockCategoryProduct(
            Long categoryProductId,
            Category mockCategory,
            Product mockProduct
    ) {
        CategoryProduct categoryProduct = CategoryProduct
                .builder()
                .id(categoryProductId)
                .category(mockCategory)
                .product(mockProduct)
                .build();
        mockCategory.getCategoryProducts().add(categoryProduct);
        mockProduct.getCategoryProducts().add(categoryProduct);
        return categoryProduct;
    }
}
