package com.whataburger.whataburgerproject.domain.options;

import com.whataburger.whataburgerproject.domain.Option;
import com.whataburger.whataburgerproject.domain.enums.AmountType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("SAUCE")
public class SauceOption extends Option {
    private AmountType amount;

    @Override
    public void validate() {

    }

    @Override
    public void apply() {

    }
}
