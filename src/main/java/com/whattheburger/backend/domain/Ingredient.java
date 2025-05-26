package com.whattheburger.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn
@Getter
public class Ingredient {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ingredient_id")
    private Long id;
//    @OneToMany(mappedBy = "ingredient")
//    private List<ProductIngredient> productIngredient = new ArrayList<>();
    private String name;
//    private Allergens
//    private Nutrition
}


/*
===product===
M:N
Product - ProductIngredient - Ingredient
whataburger
===vegee===
- lettuce
- tomato
- pickle
- onion
===meat===
- meat petty
===sauce===
- mustard
===bread===
- bun
 */