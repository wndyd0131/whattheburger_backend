package com.whattheburger.backend.domain;

import com.whattheburger.backend.domain.enums.DeltaType;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class StoreQuantityDelta {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "store_option_delta_id")
    private Long id;
    @Column(precision = 10, scale = 2)
    private BigDecimal overridePrice;
    @Enumerated(EnumType.STRING)
    private DeltaType deltaType;

    @ManyToOne
    @JoinColumn(name = "product_option_option_quantity_id")
    private ProductOptionOptionQuantity productOptionOptionQuantity;

    @ManyToOne
    @JoinColumn(name = "store_product_id")
    private StoreProduct storeProduct;
}
