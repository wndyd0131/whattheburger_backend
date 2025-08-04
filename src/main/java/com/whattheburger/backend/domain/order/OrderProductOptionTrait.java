package com.whattheburger.backend.domain.order;

import com.whattheburger.backend.domain.enums.OptionTraitType;
import com.whattheburger.backend.domain.order.OrderProductOption;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class OrderProductOptionTrait {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_product_option_trait_id")
    private Long id;
    private Long productOptionTraitId;
    private String name;
    private String labelCode;
    @Column(precision = 10, scale = 2)
    private BigDecimal calculatedPrice;
    private Double calculatedCalories;
    @Enumerated(EnumType.STRING)
    private OptionTraitType optionTraitType;
    private Integer selectedValue;

    @ManyToOne
    @JoinColumn(name = "order_product_option_id")
    private OrderProductOption orderProductOption;

    public void assignOrderProductOption(OrderProductOption orderProductOption) {
        this.orderProductOption = orderProductOption;
    }
}
