package com.whattheburger.backend.domain;

import com.whattheburger.backend.domain.enums.QuantityType;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class Quantity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quantity_id")
    private Long id;
    private QuantityType label;
    private String value;

    @OneToMany(mappedBy = "quantity")
    private List<OptionQuantity> optionQuantities = new ArrayList<>();
}
