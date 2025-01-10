package com.whataburger.whataburgerproject.domain;

import com.whataburger.whataburgerproject.controller.dto.ProductCreateRequestDTO;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

import static com.whataburger.whataburgerproject.controller.dto.ProductCreateRequestDTO.*;

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
    @ManyToMany(mappedBy = "products")
    private List<Category> categories = new ArrayList<>();
    //    @OneToMany(mappedBy = "product")
//    private List<ProductIngredient> productIngredients = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    private List<ProductOption> productOptions = new ArrayList<>();



    public Product(
            String name,
            double price,
            String ingredientInfo,
            String imageSource
    ) {
        this.name = name;
        this.price = price;
        this.ingredientInfo = ingredientInfo;
        this.imageSource = imageSource;
    }
}
