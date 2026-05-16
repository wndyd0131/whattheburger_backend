package com.whattheburger.backend.domain;
import com.whattheburger.backend.domain.enums.CountType;
import com.whattheburger.backend.domain.order.OrderProductOption;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ProductOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_option_id")
    private Long id;
    private Boolean isDefault;
    @Enumerated(EnumType.STRING)
    private CountType countType;
    private Integer defaultQuantity;
    private Integer maxQuantity;
    @Column(precision = 10, scale = 2)
    private BigDecimal extraPrice;
    private Integer orderIndex;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "option_id")
    private Option option;

    @OneToMany(mappedBy = "productOption")
    private List<ProductOptionTrait> productOptionTraits = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "custom_rule_id")
    private CustomRule customRule;

    @OneToMany(mappedBy = "productOption")
    private List<ProductOptionOptionQuantity> productOptionOptionQuantities = new ArrayList<>();

    @OneToMany(mappedBy = "productOption")
    private List<StoreOptionDelta> storeOptionDeltas = new ArrayList<>();

    public ProductOption(
            Product product,
            Option option,
            CustomRule customRule,
            Boolean isDefault,
            CountType countType,
            Integer defaultQuantity,
            Integer maxQuantity,
            BigDecimal extraPrice,
            Integer orderIndex
    ) {
        this.product = product;
        this.option = option;
        this.customRule = customRule;
        this.isDefault = isDefault;
        this.countType = countType;
        this.defaultQuantity = defaultQuantity;
        this.maxQuantity = maxQuantity;
        this.extraPrice = extraPrice;
        this.orderIndex = orderIndex;
    }
}
