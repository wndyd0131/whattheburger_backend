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
        @UniqueConstraint(columnNames = {"option_id", "ingredient_id"})
})
public class OptionIngredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_ingredient_id")
    private Long id;
    private Integer requiredQuantity;

    @ManyToOne
    @JoinColumn(name = "option_id")
    private Option option;

    @ManyToOne
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    public OptionIngredient(
            Option option,
            Ingredient ingredient,
            Integer requiredQuantity
    ) {
        this.option = option;
        this.ingredient = ingredient;
        this.requiredQuantity = requiredQuantity;
    }
}
