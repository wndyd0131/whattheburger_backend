package com.whattheburger.backend.domain;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
public class StoreProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_product_id")
    private Long id;
    @Column(precision = 10, scale = 2)
    private BigDecimal overridePrice;
    private Boolean isActive;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @OneToMany(mappedBy = "storeProduct")
    private List<StoreOptionDelta> storeOptionDeltas = new ArrayList<>();

    @OneToMany(mappedBy = "storeProduct")
    private List<StoreTraitDelta> storeTraitDeltas = new ArrayList<>();

    @OneToMany(mappedBy = "storeProduct")
    private List<StoreQuantityDelta> storeQuantityDeltas = new ArrayList<>();

    public StoreProduct(BigDecimal overridePrice, Store store, Product product, List<ProductOption> productOptions) {
        this.store = store;
        this.product = product;
        this.overridePrice = overridePrice;
    }
}
