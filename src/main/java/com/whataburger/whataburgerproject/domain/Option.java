package com.whataburger.whataburgerproject.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
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

    @OneToMany(mappedBy = "option")
    private List<OptionOptionTrait> optionOptionTraits = new ArrayList<>();

    public Option(String name, String imageSource, Double calories) {
        this.name = name;
        this.imageSource = imageSource;
        this.calories = calories;
    }
}
