package com.whattheburger.backend.domain.order;

import com.whattheburger.backend.domain.enums.CountType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class OrderProductOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_product_option_id")
    private Long id;
    private Long productOptionId;
    private String name;
    @Enumerated(EnumType.STRING)
    private CountType countType;
    @Column(precision = 10, scale = 2)
    private BigDecimal totalPrice;
    @Column(precision = 10, scale = 2)
    private BigDecimal basePrice;
    private Double totalCalories;
    private Integer quantity;
    @Embedded
    private QuantityDetail quantityDetail;

    @ManyToOne
    @JoinColumn(name = "order_custom_rule_id")
    private OrderCustomRule orderCustomRule;

    @OneToMany(mappedBy = "orderProductOption", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderProductOptionTrait> orderProductOptionTraits = new ArrayList<>();

    public void assignOrderCustomRule(OrderCustomRule orderCustomRule) {
        this.orderCustomRule = orderCustomRule;
    }
    public void assignOrderProductOptionTraits(List<OrderProductOptionTrait> orderProductOptionTraits) {
        this.orderProductOptionTraits.clear();
        this.orderProductOptionTraits.addAll(orderProductOptionTraits);
    }
}