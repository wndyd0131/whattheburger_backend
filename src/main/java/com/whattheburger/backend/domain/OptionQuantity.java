package com.whattheburger.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class OptionQuantity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_quantity_id")
    private Long id;
    private Double extraPrice;
    private Double extraCalories;

    @ManyToOne
    @JoinColumn(name = "option_id")
    private Option option;

    @ManyToOne
    @JoinColumn(name = "quantity_id")
    private Quantity quantity;
}
