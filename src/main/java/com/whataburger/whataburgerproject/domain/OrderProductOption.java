package com.whataburger.whataburgerproject.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class OrderProductOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_product_option_id")
    private Long id;
    private String name;
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "order_product_id")
    private OrderProduct orderProduct;

    @ManyToOne
    @JoinColumn(name = "product_option_id")
    private ProductOption productOption;

    @OneToMany(mappedBy = "orderProductOption")
    private List<OrderProductOptionTrait> orderProductOptionTraits = new ArrayList<>();
}