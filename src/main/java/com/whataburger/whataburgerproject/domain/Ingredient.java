package com.whataburger.whataburgerproject.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn
public class Ingredient {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ingredient_id")
    private Long id;
//    @OneToMany(mappedBy = "ingredient")
//    private List<ProductIngredient> productIngredient = new ArrayList<>();
    private String name;
    private String amount; //count or oz
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