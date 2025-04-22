package com.whataburger.whataburgerproject.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class ProductOptionTrait {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_option_trait_id")
    private Long id;
    private Integer defaultSelection;
    private Double extraPrice;
    private Double extraCalories;

    @ManyToOne
    @JoinColumn(name = "product_option_id")
    private ProductOption productOption;

    @ManyToOne
    @JoinColumn(name = "option_trait_id")
    private OptionTrait optionTrait;

    @OneToMany(mappedBy = "productOptionTrait")
    private List<OrderProductOptionTrait> orderProductOptionTraits = new ArrayList<>();

    public ProductOptionTrait (
            ProductOption productOption,
            OptionTrait optionTrait,
            Integer defaultSelection,
            Double extraPrice,
            Double extraCalories
    ) {
        this.productOption = productOption;
        this.optionTrait = optionTrait;
        this.defaultSelection = defaultSelection;
        this.extraPrice = extraPrice;
        this.extraCalories = extraCalories;
    }
}
