package com.whataburger.whataburgerproject.domain.options;

import com.whataburger.whataburgerproject.domain.Option;
import com.whataburger.whataburgerproject.domain.enums.AmountType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
@DiscriminatorValue("TOPPING")
public class ToppingOption extends Option {
    @Enumerated(EnumType.STRING)
    private AmountType amount;

    @Override
    public void validate() {

    }

    @Override
    public void apply() {

    }
}
