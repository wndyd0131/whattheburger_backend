package com.whattheburger.backend.domain.temp;

import jakarta.persistence.*;

//@Entity
public class OptionIngredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="option_ingredient_id")
    private Long id;

//    @ManyToOne
//    private Option
}
