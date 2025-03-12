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
    private double price;
    private String briefInfo;
    private String imageSource;
    private double calories;
    @Enumerated(EnumType.STRING)
    private ProductType productType;

    @ManyToMany(mappedBy = "products")
    private List<Category> categories = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    private List<ProductOption> productOptions = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    private List<OrderProduct> orderProducts = new ArrayList<>();

    public Product(
            String name,
            double price,
            String briefInfo,
            String imageSource,
            double calories,
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
