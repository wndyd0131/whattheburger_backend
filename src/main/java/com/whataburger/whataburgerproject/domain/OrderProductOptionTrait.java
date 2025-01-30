package com.whataburger.whataburgerproject.domain;

import jakarta.persistence.*;

@Entity
public class OrderProductOptionTrait {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_product_option_trait_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_product_option_id")
    private OrderProductOption orderProductOption;

    @ManyToOne
    @JoinColumn(name = "option_trait_id")
    private OptionTrait optionTrait;
}
