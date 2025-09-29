package com.whattheburger.backend.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class StoreTraitDelta {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "store_trait_delta_id")
    private Long id;
    @Column(precision = 10, scale = 2)
    private BigDecimal overridePrice;
    @Enumerated(EnumType.STRING)
    private DeltaType deltaType;

    @ManyToOne
    @JoinColumn(name = "product_option_trait_id")
    private ProductOptionTrait productOptionTrait;

    @ManyToOne
    @JoinColumn(name = "store_product_id")
    private StoreProduct storeProduct;
}
