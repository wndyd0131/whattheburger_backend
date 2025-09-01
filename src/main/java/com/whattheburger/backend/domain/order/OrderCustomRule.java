package com.whattheburger.backend.domain.order;

import com.whattheburger.backend.domain.enums.CustomRuleType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderCustomRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_custom_rule_id")
    private Long id;
    private Long customRuleId;
    private String name;
    @Column(precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @ManyToOne
    @JoinColumn(name = "order_product_id")
    private OrderProduct orderProduct;

    @OneToMany(mappedBy = "orderCustomRule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderProductOption> orderProductOptions = new ArrayList<>();

    public void assignOrderProduct(OrderProduct orderProduct) {
        this.orderProduct = orderProduct;
    }
    public void assignOrderProductOptions(List<OrderProductOption> orderProductOptions) {
        this.orderProductOptions.clear();
        this.orderProductOptions.addAll(orderProductOptions);
    }
}
