package com.whattheburger.backend.domain.order;

import com.whattheburger.backend.domain.Product;
import com.whattheburger.backend.domain.enums.ProductType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class OrderProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_product_id")
    private Long id;
    private Long storeProductId;
    private Integer quantity;
    private String forWhom;
    private String name;
    @Column(precision = 10, scale = 2)
    private BigDecimal totalPrice;
    @Column(precision = 10, scale = 2)
    private BigDecimal extraPrice;
    @Column(precision = 10, scale = 2)
    private BigDecimal basePrice;
    private String imageSource;
    private Double totalCalories;
    @Enumerated(EnumType.STRING)
    private ProductType productType;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @OneToMany(mappedBy = "orderProduct", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderCustomRule> orderCustomRules = new ArrayList<>();

    public void assignOrder(Order order) {
        this.order = order;
    }
    public void assignOrderCustomRules(List<OrderCustomRule> orderCustomRules) {
        this.orderCustomRules.clear();
        this.orderCustomRules.addAll(orderCustomRules);
    }
}
