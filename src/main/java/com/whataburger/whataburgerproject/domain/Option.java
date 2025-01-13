package com.whataburger.whataburgerproject.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "options")
public class Option {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id")
    private Long id;
    private String name;
    private double extraPrice;

    @ManyToOne
    @JoinColumn(name = "option_type_id")
    private OptionType optionType;

    @OneToMany(mappedBy = "option")
    private List<ProductOption> productOptions = new ArrayList<>();

    @ManyToMany
    @JoinTable(name="OPTION_OPTION_TRAIT",
            joinColumns = @JoinColumn(name="option_id"),
            inverseJoinColumns = @JoinColumn(name="option_trait_id"))
    private List<OptionTrait> optionTraits = new ArrayList<>();
}
