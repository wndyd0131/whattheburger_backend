package com.whataburger.whataburgerproject.domain;
import com.whataburger.whataburgerproject.domain.enums.ProductType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;
    private String name;
    private Double price;
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

    public Product(
            String name,
            Double price,
            String briefInfo,
            String imageSource,
            Double calories,
            ProductType productType
    ) {
        this.name = name;
        this.price = price;
        this.briefInfo = briefInfo;
        this.imageSource = imageSource;
        this.calories = calories;
        this.productType = productType;
    }
}
