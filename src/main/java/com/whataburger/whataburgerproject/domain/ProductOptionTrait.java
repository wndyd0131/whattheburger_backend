package com.whataburger.whataburgerproject.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class ProductOptionTrait {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_option_trait_id")
    private Long id;
    private Boolean isDefault;
    private double extraPrice;
    private int extraCalories;

    @ManyToOne
    @JoinColumn(name = "product_option_id")
    private ProductOption productOption;

    @ManyToOne
    @JoinColumn(name = "option_trait_id")
    private OptionTrait optionTrait;

    @OneToMany(mappedBy = "productOptionTrait")
    private List<OrderProductOptionTrait> orderProductOptionTraits = new ArrayList<>();
}
