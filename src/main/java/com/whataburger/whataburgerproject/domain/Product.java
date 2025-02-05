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
    @Id
    @Column(name = "product_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private double price;
    private String ingredientInfo;
    private String imageSource;
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
            String ingredientInfo,
            String imageSource,
            ProductType productType
    ) {
        this.name = name;
        this.price = price;
        this.ingredientInfo = ingredientInfo;
        this.imageSource = imageSource;
        this.productType = productType;
    }
}
