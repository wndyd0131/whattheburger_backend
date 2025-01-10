package com.whataburger.whataburgerproject.domain.options;

import com.whataburger.whataburgerproject.domain.Option;
import com.whataburger.whataburgerproject.domain.enums.DrinkSize;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
@DiscriminatorValue("DRINK")
public class DrinkOption extends Option {
    @Enumerated(EnumType.STRING)
    private DrinkSize drinkSize;

    @Override
    public void validate() {
    }

    @Override
    public void apply() {
    }
}
