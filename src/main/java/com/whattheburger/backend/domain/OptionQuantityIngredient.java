package com.whattheburger.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"option_quantity_id", "ingredient_id"})
})
public class OptionQuantityIngredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_quantity_ingredient_id")
    private Long id;
    private Integer requiredQuantity;

    @ManyToOne
    @JoinColumn(name = "option_quantity_id")
    private OptionQuantity optionQuantity;

    @ManyToOne
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    public OptionQuantityIngredient(
            OptionQuantity optionQuantity,
            Ingredient ingredient,
            Integer requiredQuantity
    ) {
        this.optionQuantity = optionQuantity;
        this.ingredient = ingredient;
        this.requiredQuantity = requiredQuantity;
    }
}
