package com.whataburger.whataburgerproject.domain;

import com.whataburger.whataburgerproject.domain.enums.CustomRuleType;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class CustomRule {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "custom_rule_id")
    private Long id;
    private String name;
    @Enumerated(EnumType.STRING)
    private CustomRuleType customRuleType;
    private int rowIndex;
    private int min_selection;
    private int max_selection;

    @OneToMany(mappedBy = "customRule")
    private List<ProductOption> productOptions = new ArrayList<>();
}
