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
    private int calories;

    @OneToMany(mappedBy = "option")
    private List<ProductOption> productOptions = new ArrayList<>();

    @ManyToMany
    @JoinTable(name="OPTION_OPTION_TRAIT",
            joinColumns = @JoinColumn(name="option_id"),
            inverseJoinColumns = @JoinColumn(name="option_trait_id"))
    private List<OptionTrait> optionTraits = new ArrayList<>();

    public Option(String name, String imageSource, int calories) {
        this.name = name;
        this.imageSource = imageSource;
        this.calories = calories;
    }
}
