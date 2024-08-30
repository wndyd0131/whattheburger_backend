package com.whataburger.whataburgerproject.domain;

import jakarta.persistence.*;

@Entity
public class ProductIngredient {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_ingredient_id")
    private Long id;

//    @ManyToOne
//    @JoinColumn(name = "product_id")
//    private Product product;
//    @ManyToOne
//    @JoinColumn(name = "ingredient_id")
//    private Ingredient ingredient;
}
