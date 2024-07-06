package com.whataburger.whataburgerproject.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Product {
    @Id
    @Column(name = "product_id")
    @GeneratedValue
    private Long id;
    private String name;
    private int price;
    private int stock;
    @OneToMany //?
    private List<Ingredient> ingredients = new ArrayList<>();
    private List categories;
}
