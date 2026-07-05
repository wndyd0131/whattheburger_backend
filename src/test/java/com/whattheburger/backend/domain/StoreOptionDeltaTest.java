package com.whattheburger.backend.domain;

import com.whattheburger.backend.domain.enums.CountType;
import com.whattheburger.backend.domain.enums.DeltaType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class StoreOptionDeltaTest {

    @Test
    void resolveExtraPrice_whenDeltaIsNull_returnsProductOptionPrice() {
        ProductOption productOption = productOptionWithPrice(BigDecimal.valueOf(1.99));

        Optional<BigDecimal> result = StoreOptionDelta.resolveExtraPrice(productOption, null);

        assertThat(result).contains(BigDecimal.valueOf(1.99));
    }

    @Test
    void resolveExtraPrice_whenOverride_returnsOverridePrice() {
        ProductOption productOption = productOptionWithPrice(BigDecimal.valueOf(1.99));
        StoreOptionDelta delta = new StoreOptionDelta(productOption, null);
        delta.override(BigDecimal.valueOf(3.49), DeltaType.OVERRIDE);

        Optional<BigDecimal> result = StoreOptionDelta.resolveExtraPrice(productOption, delta);

        assertThat(result).contains(BigDecimal.valueOf(3.49));
    }

    @Test
    void resolveExtraPrice_whenHidden_returnsEmpty() {
        ProductOption productOption = productOptionWithPrice(BigDecimal.valueOf(1.99));
        StoreOptionDelta delta = new StoreOptionDelta(productOption, null);
        delta.override(null, DeltaType.HIDDEN);

        Optional<BigDecimal> result = StoreOptionDelta.resolveExtraPrice(productOption, delta);

        assertThat(result).isEmpty();
    }

    private ProductOption productOptionWithPrice(BigDecimal extraPrice) {
        return ProductOption.builder()
                .id(1L)
                .extraPrice(extraPrice)
                .countType(CountType.COUNTABLE)
                .build();
    }
}
