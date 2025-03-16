package com.whataburger.whataburgerproject.domain;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
public class ProductOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_option_id")
    private Long id;
    private Boolean isDefault;
    private int defaultQuantity;
    private int maxQuantity;
    private double extraPrice;
    private int orderIndex;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "option_id")
    private Option option;

    @OneToMany(mappedBy = "productOption")
    private List<ProductOptionTrait> productOptionTraits = new ArrayList<>();

    @OneToMany(mappedBy = "productOption")
    private List<OrderProductOption> orderProductOptions = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "custom_rule_id")
    private CustomRule customRule;

    public ProductOption(
            Product product,
            Option option,
            Boolean isDefault,
            int defaultQuantity,
            int maxQuantity,
            double extraPrice,
            int orderIndex
    ) {
        this.product = product;
        this.option = option;
        this.isDefault = isDefault;
        this.defaultQuantity = defaultQuantity;
        this.maxQuantity = maxQuantity;
        this.extraPrice = extraPrice;
        this.orderIndex = orderIndex;
    }
}
