package com.whattheburger.backend.domain;

import com.whattheburger.backend.domain.enums.DeltaType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Optional;

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

    public static Optional<BigDecimal> resolveExtraPrice(ProductOption productOption, StoreOptionDelta delta) {
        if (delta == null) {
            return Optional.of(productOption.getExtraPrice());
        }
        if (delta.getDeltaType() == DeltaType.OVERRIDE) {
            return Optional.of(delta.getOverridePrice());
        }
        return Optional.empty();
    }
}
