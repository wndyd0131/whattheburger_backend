package com.whataburger.whataburgerproject.domain.options;

import com.whataburger.whataburgerproject.domain.Option;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("BREAD")
public class BreadOption extends Option {
    private boolean toasted;

    @Override
    public void validate() {

    }

    @Override
    public void apply() {

    }
}
