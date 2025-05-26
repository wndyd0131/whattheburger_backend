package com.whattheburger.backend.domain;

import jakarta.persistence.*;

@Entity
public class OptionType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_type_id")
    private Long id;
    private String name;
}
