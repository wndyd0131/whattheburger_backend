package com.whattheburger.backend.domain;

import jakarta.persistence.*;

@Entity
public class OptionOptionTrait {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_option_trait_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "option_id")
    private Option option;

    @ManyToOne
    @JoinColumn(name = "option_trait_id")
    private OptionTrait optionTrait;
}
