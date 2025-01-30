package com.whataburger.whataburgerproject.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class OptionTrait {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_trait_id")
    private Long id;
    private String name;

    @ManyToMany(mappedBy = "optionTraits") // toasted, not-toasted
    private List<Option> options = new ArrayList<>();

    @OneToMany(mappedBy = "optionTrait")
    private List<ProductOptionTrait> productOptionTraits = new ArrayList<>();

    @OneToMany(mappedBy = "optionTrait")
    private List<OrderProductOptionTrait> orderProductOptionTraits = new ArrayList<>();
}
