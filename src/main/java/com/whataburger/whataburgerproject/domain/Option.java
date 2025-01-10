package com.whataburger.whataburgerproject.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "option_type", discriminatorType = DiscriminatorType.STRING)
@Table(name="options")
public abstract class Option {
    @Id
    @GeneratedValue
    @Column(name = "option_id")
    private Long id;
    private String name;
    private double extraPrice;
    private int calories;
    @OneToMany(mappedBy = "option")
    private List<ProductOption> productOptions = new ArrayList<>();
    //    @OneToMany
//    private Ingredient ingredient;

    public abstract void validate();
    public abstract void apply();
}
