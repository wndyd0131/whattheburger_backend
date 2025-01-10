package com.whataburger.whataburgerproject.domain.options;

import com.whataburger.whataburgerproject.domain.Option;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("BEEF")
public class BeefOption extends Option {
    private int quantity;
    private int maxQuantity;

    @Override
    public void validate() {

    }

    @Override
    public void apply() {

    }
}
