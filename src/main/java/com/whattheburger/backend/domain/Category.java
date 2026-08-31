package com.whattheburger.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;
    private String name;
    private Integer orderIndex;

    @ManyToOne
    @JoinColumn(name = "PARENT_ID")
    private Category parent;

    @Builder.Default
    @OneToMany(mappedBy = "parent")
    private List<Category> children = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "category")
    private List<CategoryProduct> categoryProducts = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "category")
    private List<CategoryStoreProduct> categoryStoreProducts = new ArrayList<>();

    public Category(String name) {
        this.name = name;
    }
}
