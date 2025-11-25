package com.whattheburger.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"store_id", "product_id"})
})
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

    @OneToMany(mappedBy = "storeProduct")
    private List<CategoryStoreProduct> categoryStoreProducts = new ArrayList<>();

    public StoreProduct(Store store, Product product) {
        this.store = store;
        this.product = product;
        this.isActive = true;
    }

    public void changePrice(BigDecimal price) {
        this.overridePrice = price;
    }
}
