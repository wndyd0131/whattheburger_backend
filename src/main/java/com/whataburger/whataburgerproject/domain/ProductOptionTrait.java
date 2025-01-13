package com.whataburger.whataburgerproject.domain;

import jakarta.persistence.*;

@Entity
public class ProductOptionTrait {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_option_trait_id")
    private Long id;
    private Boolean isDefault;

    @ManyToOne
    @JoinColumn(name = "product_option_id")
    private ProductOption productOption;

    @ManyToOne
    @JoinColumn(name = "option_trait_id")
    private OptionTrait optionTrait;
}
