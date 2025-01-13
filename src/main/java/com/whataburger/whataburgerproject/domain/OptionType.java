package com.whataburger.whataburgerproject.domain;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class OptionType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_type_id")
    private Long id;
    private String name;

    @OneToMany(mappedBy = "optionType")
    private List<Option> options;
}
