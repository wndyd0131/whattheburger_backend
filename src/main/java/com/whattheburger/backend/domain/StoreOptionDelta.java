package com.whattheburger.backend.domain;

import com.whattheburger.backend.domain.enums.DeltaType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@NoArgsConstructor
@Getter
public class StoreOptionDelta {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "store_option_delta_id")
    private Long id;
    @Column(precision = 10, scale = 2)
    private BigDecimal overridePrice;
    @Enumerated(EnumType.STRING)
    private DeltaType deltaType;

    @ManyToOne
    @JoinColumn(name = "product_option_id")
    private ProductOption productOption;

    @ManyToOne
    @JoinColumn(name = "store_product_id")
    private StoreProduct storeProduct;

    public StoreOptionDelta(
            ProductOption productOption,
            StoreProduct storeProduct
    ) {
        this.productOption = productOption;
        this.storeProduct = storeProduct;
    }

    public void override(BigDecimal price, DeltaType deltaType) {
        this.overridePrice = price;
        this.deltaType = deltaType;
    }
}
