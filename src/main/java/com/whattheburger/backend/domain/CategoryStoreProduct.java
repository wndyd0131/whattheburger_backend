package com.whattheburger.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryStoreProduct {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_category_product_id")
    private Long id;
    private Integer orderIndex;

    @ManyToOne
    @JoinColumn(name = "store_product_id")
    private StoreProduct storeProduct;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    public CategoryStoreProduct(
            StoreProduct storeProduct,
            Category category
    ) {
        this.orderIndex = 0;
        this.storeProduct = storeProduct;
        this.category = category;
    }
}
