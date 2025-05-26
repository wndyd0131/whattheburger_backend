package com.whataburger.whataburgerproject.domain;
import com.whataburger.whataburgerproject.domain.enums.CountType;
import com.whataburger.whataburgerproject.domain.enums.MeasureType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
public class ProductOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_option_id")
    private Long id;
    private Boolean isDefault;
    private CountType countType;
    private MeasureType measureType;
    private Integer defaultQuantity;
    private Integer maxQuantity;
    private Double extraPrice;
    private Integer orderIndex;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "option_id")
    private Option option;

    @OneToMany(mappedBy = "productOption")
    private List<ProductOptionTrait> productOptionTraits = new ArrayList<>();

    @OneToMany(mappedBy = "productOption")
    private List<OrderProductOption> orderProductOptions = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "custom_rule_id")
    private CustomRule customRule;

    public ProductOption(
            Product product,
            Option option,
            CustomRule customRule,
            Boolean isDefault,
            CountType countType,
            MeasureType measureType,
            Integer defaultQuantity,
            Integer maxQuantity,
            Double extraPrice,
            Integer orderIndex
    ) {
        this.product = product;
        this.option = option;
        this.customRule = customRule;
        this.isDefault = isDefault;
        this.countType = countType;
        this.measureType = measureType;
        this.defaultQuantity = defaultQuantity;
        this.maxQuantity = maxQuantity;
        this.extraPrice = extraPrice;
        this.orderIndex = orderIndex;
    }
}
