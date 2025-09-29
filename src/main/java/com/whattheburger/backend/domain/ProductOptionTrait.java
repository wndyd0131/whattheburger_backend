package com.whattheburger.backend.domain;

import com.whattheburger.backend.domain.order.OrderProductOptionTrait;
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
public class ProductOptionTrait {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_option_trait_id")
    private Long id;
    private Integer defaultSelection;
    @Column(precision = 10, scale = 2)
    private BigDecimal extraPrice;
    private Double extraCalories;

    @ManyToOne
    @JoinColumn(name = "product_option_id")
    private ProductOption productOption;

    @ManyToOne
    @JoinColumn(name = "option_trait_id")
    private OptionTrait optionTrait;

    @OneToMany(mappedBy = "productOptionTrait")
    private List<StoreTraitDelta> storeTraitDeltas = new ArrayList<>();

    public ProductOptionTrait (
            ProductOption productOption,
            OptionTrait optionTrait,
            Integer defaultSelection,
            BigDecimal extraPrice,
            Double extraCalories
    ) {
        this.productOption = productOption;
        this.optionTrait = optionTrait;
        this.defaultSelection = defaultSelection;
        this.extraPrice = extraPrice;
        this.extraCalories = extraCalories;
    }
}
