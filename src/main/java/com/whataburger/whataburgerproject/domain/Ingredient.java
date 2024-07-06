package com.whataburger.whataburgerproject.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Ingredient {
    @Id @GeneratedValue
    @Column(name = "ingredient_id")
    private Long id;
    private String name;
    private String amount; //count or oz
}
