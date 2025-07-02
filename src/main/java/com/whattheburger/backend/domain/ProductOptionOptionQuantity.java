package com.whattheburger.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class ProductOptionOptionQuantity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_option_option_quantity_id")
    private Long id;
    private Double extraPrice;
    private Boolean isDefault;

    @ManyToOne
    @JoinColumn(name = "product_option_id")
    private ProductOption productOption;
    @ManyToOne
    @JoinColumn(name = "option_quantity_id")
    private OptionQuantity optionQuantity;

    public ProductOptionOptionQuantity(ProductOption productOption, OptionQuantity optionQuantity, Double extraPrice, Boolean isDefault) {
        this.productOption = productOption;
        this.optionQuantity = optionQuantity;
        this.extraPrice = extraPrice;
        this.isDefault = isDefault;
    }
}
