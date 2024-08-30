package com.whataburger.whataburgerproject.domain;

import jakarta.persistence.*;

@Entity
public class Allergens {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "allergen_id")
    private Long id;
    private String allergen_name;
}
