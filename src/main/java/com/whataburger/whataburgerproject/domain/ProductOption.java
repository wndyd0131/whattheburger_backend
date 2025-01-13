package com.whataburger.whataburgerproject.domain;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
public class ProductOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_option_id")
    private Long id;
    private Boolean isDefault;
    private int defaultQuantity;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "option_id")
    private Option option;

    @OneToMany(mappedBy = "productOption")
    private List<ProductOptionTrait> productOptionTraits = new ArrayList<>();

    public ProductOption(
            Product product,
            Option option,
            Boolean isDefault,
            int defaultQuantity
    ) {
        this.product = product;
        this.option = option;
        this.isDefault = isDefault;
        this.defaultQuantity = defaultQuantity;
    }
}
