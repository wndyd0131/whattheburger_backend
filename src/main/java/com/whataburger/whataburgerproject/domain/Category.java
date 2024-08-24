package com.whataburger.whataburgerproject.domain;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;
    private String name;
}
