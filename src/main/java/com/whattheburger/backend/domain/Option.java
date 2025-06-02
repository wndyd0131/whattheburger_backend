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
@Table(name = "options")
@NoArgsConstructor
public class Option {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id")
    private Long id;
    private String name;
    private String imageSource;
    private Double calories;

    @OneToMany(mappedBy = "option")
    private List<ProductOption> productOptions = new ArrayList<>();

    public Option(String name, String imageSource, Double calories) {
        this.name = name;
        this.imageSource = imageSource;
        this.calories = calories;
    }
}
