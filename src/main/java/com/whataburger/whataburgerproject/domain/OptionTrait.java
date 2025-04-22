package com.whataburger.whataburgerproject.domain;

import com.whataburger.whataburgerproject.domain.enums.OptionTraitType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class OptionTrait {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_trait_id")
    private Long id;
    private String name;
    private String labelCode;
    @Enumerated(EnumType.STRING)
    private OptionTraitType optionTraitType;

    @ManyToMany(mappedBy = "optionTraits") // toasted, not-toasted
    private List<Option> options = new ArrayList<>();

    @OneToMany(mappedBy = "optionTrait")
    private List<ProductOptionTrait> productOptionTraits = new ArrayList<>();

    public OptionTrait(String name, String labelCode, OptionTraitType optionTraitType) {
        this.name = name;
        this.labelCode = labelCode;
        this.optionTraitType = optionTraitType;
    }
}
