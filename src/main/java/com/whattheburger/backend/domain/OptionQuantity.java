package com.whattheburger.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptionQuantity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_quantity_id")
    private Long id;
    private Double extraCalories;

    @ManyToOne
    @JoinColumn(name = "option_id")
    private Option option;

    @ManyToOne
    @JoinColumn(name = "quantity_id")
    private Quantity quantity;

    @OneToMany(mappedBy = "optionQuantity")
    @Builder.Default
    private List<ProductOptionOptionQuantity> productOptionOptionQuantities = new ArrayList<>();

    @OneToMany(mappedBy = "optionQuantity")
    @Builder.Default
    private List<OptionQuantityIngredient> optionQuantityIngredients = new ArrayList<>();
}
