package com.whataburger.whataburgerproject.domain;

import com.whataburger.whataburgerproject.domain.enums.Allergens;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class Nutrition {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nutrition_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private int weight;
    private int calories;
    private int caloriesFromFat;
    private int saturatedFat;
    private int transFat;
    private int cholesterol;
    private int sodium;
    private int dietaryFiber;
    private int protein;
    private int sugars;
    @ElementCollection
    @Enumerated(EnumType.STRING)
    private List<Allergens> allergens;

    /* allergens
    * 1. column: allergen_name, product_id
    * 2. column: soy, wheat..., product_id (each column has boolean value)
    * */
}
