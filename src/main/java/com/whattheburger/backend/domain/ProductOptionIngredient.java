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
        @UniqueConstraint(columnNames = {"product_option_id", "ingredient_id"})
})
public class ProductOptionIngredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_option_ingredient_id")
    private Long id;
    private Integer requiredQuantity;

    @ManyToOne
    @JoinColumn(name = "product_option_id")
    private ProductOption productOption;

    @ManyToOne
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    public ProductOptionIngredient(
            ProductOption productOption,
            Ingredient ingredient,
            Integer requiredQuantity
    ) {
        this.productOption = productOption;
        this.ingredient = ingredient;
        this.requiredQuantity = requiredQuantity;
    }
}
