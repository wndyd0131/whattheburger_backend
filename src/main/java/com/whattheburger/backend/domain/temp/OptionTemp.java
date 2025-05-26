package com.whattheburger.backend.domain.temp;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

//@Entity
@Table(name = "options")
public class OptionTemp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="option_id")
    private Long id;
    private String name; // Large bun, Brioche, Tomatoes, Egg...
    private double extraPrice;
    private int calories;
    @ManyToMany
    @JoinTable(name = "OPTION_OPTION_GROUP",
            joinColumns = @JoinColumn(name="option_id"),
            inverseJoinColumns = @JoinColumn(name="option_group_id")
    )
    private List<OptionGroup> optionGroups = new ArrayList<>();
}

/**
 * large_bun
 * small_bun
 * brioche_bun
 * texas_toast
 * no_bun
 * large_beef_patty
 * small_beef_patty
 * american_cheese
 * montery_jack_cheese
 * bacon_slices
 * grilled_jalapenos
 * jalapenos
 * avocado
 * grilled_peppers_and_onions
 * tomato
 * lettuce
 * pickle
 * diced_onion
 * grilled_onion
 * mustard
 * mayonnaise
 * ketchup
 * honey_bbq
 * creamy_pepper
 *
 */