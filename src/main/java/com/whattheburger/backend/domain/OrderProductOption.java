package com.whattheburger.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderProductOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_product_option_id")
    private Long id;
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "order_product_id")
    private OrderProduct orderProduct;

    @ManyToOne
    @JoinColumn(name = "product_option_id")
    private ProductOption productOption;

    @OneToMany(mappedBy = "orderProductOption")
    private List<OrderProductOptionTrait> orderProductOptionTraits = new ArrayList<>();

    public OrderProductOption(Integer quantity, OrderProduct orderProduct, ProductOption productOption) {
        this.quantity = quantity;
        this.orderProduct = orderProduct;
        this.productOption = productOption;
    }
}