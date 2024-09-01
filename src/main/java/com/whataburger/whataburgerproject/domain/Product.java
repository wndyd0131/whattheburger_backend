package com.whataburger.whataburgerproject.domain;

import com.whataburger.whataburgerproject.exception.NotEnoughStockException;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {
    @Id
    @Column(name = "product_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private double price;
    private String ingredientInfo;
//    @OneToMany(mappedBy = "product")
//    private List<ProductIngredient> productIngredients = new ArrayList<>();

    public Product(
            String name,
            double price,
            String ingredientInfo
    ) {
        this.name = name;
        this.price = price;
        this.ingredientInfo = ingredientInfo;
    }

}
