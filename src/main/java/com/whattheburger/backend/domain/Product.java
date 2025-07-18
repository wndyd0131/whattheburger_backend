package com.whattheburger.backend.domain;
import com.whattheburger.backend.domain.enums.ProductType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;
    private String name;
    @Column(precision = 10, scale = 2)
    private BigDecimal price;
    private String briefInfo;
    private String imageSource;
    private Double calories;
    @Enumerated(EnumType.STRING)
    private ProductType productType;

    @OneToMany(mappedBy = "product")
    private List<ProductOption> productOptions = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    private List<OrderProduct> orderProducts = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    private List<CategoryProduct> categoryProducts = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    private List<StoreProduct> storeProducts = new ArrayList<>();

    public Product(
            String name,
            BigDecimal price,
            String briefInfo,
            Double calories,
            ProductType productType
    ) {
        this.name = name;
        this.price = price;
        this.briefInfo = briefInfo;
        this.calories = calories;
        this.productType = productType;
    }

    public void changeImageSource(String imageSource) {
        this.imageSource = imageSource;
    }
}
