package com.whattheburger.backend.domain;

import jakarta.persistence.*;

@Entity
public class StoreProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_product_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    public StoreProduct(Store store, Product product) {
        this.store = store;
        this.product = product;
    }
}
