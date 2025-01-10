package com.whataburger.whataburgerproject.domain.temp;

import jakarta.persistence.*;

import java.util.List;

//@Entity
public class OptionGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="option_group_id")
    private Long id;
    private String name;
    @ManyToMany(mappedBy = "optionGroups")
    private List<OptionTemp> optionTemps;
}

/**
 * Bread
 * Toasting
 * Beef
 * Cheese
 *
 */
