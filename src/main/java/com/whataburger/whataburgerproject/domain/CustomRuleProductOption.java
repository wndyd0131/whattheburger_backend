package com.whataburger.whataburgerproject.domain;

import jakarta.persistence.*;

@Entity
public class CustomRuleProductOption {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "custom_rule_product_option_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_option_id")
    private ProductOption productOption;

    @ManyToOne
    @JoinColumn(name = "custom_rule_id")
    private CustomRule customRule;
}
