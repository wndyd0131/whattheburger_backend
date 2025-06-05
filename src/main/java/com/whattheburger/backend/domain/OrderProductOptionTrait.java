package com.whattheburger.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderProductOptionTrait {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_product_option_trait_id")
    private Long id;
    private Integer value;

    @ManyToOne
    @JoinColumn(name = "order_product_option_id")
    private OrderProductOption orderProductOption;

    @ManyToOne
    @JoinColumn(name = "product_option_trait_id")
    private ProductOptionTrait productOptionTrait;

    public OrderProductOptionTrait(Integer value, OrderProductOption orderProductOption, ProductOptionTrait productOptionTrait) {
        this.value = value;
        this.orderProductOption = orderProductOption;
        this.productOptionTrait = productOptionTrait;
    }
}
