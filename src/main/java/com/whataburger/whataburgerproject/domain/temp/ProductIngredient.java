package com.whataburger.whataburgerproject.domain.temp;

import com.whataburger.whataburgerproject.domain.Ingredient;
import com.whataburger.whataburgerproject.domain.Product;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class ProductIngredient {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_ingredient_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    @ManyToOne
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    public ProductIngredient(
            Product product,
            Ingredient ingredient
    ) {
        this.product = product;
        this.ingredient = ingredient;
    }
}
