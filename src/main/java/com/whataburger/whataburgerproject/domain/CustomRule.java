package com.whataburger.whataburgerproject.domain;

import com.whataburger.whataburgerproject.domain.enums.CustomRuleType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class CustomRule {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "custom_rule_id")
    private Long id;
    private String name;
    @Enumerated(EnumType.STRING)
    private CustomRuleType customRuleType;
    private int rowIndex;
    private int minSelection;
    private int maxSelection;

    @OneToMany(mappedBy = "customRule")
    private List<ProductOption> productOptions = new ArrayList<>();

    public CustomRule(
            String name,
            CustomRuleType customRuleType,
            int rowIndex,
            int minSelection,
            int maxSelection
    ) {
        this.name = name;
        this.customRuleType = customRuleType;
        this.rowIndex = rowIndex;
        this.minSelection = minSelection;
        this.maxSelection = maxSelection;
    }
}
