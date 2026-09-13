package com.whattheburger.backend.domain;

import com.whattheburger.backend.domain.enums.CountType;
import com.whattheburger.backend.domain.enums.DeltaType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StoreOptionDeltaTest {

    @Test
    void isHidden_whenDeltaIsNull_returnsFalse() {
        assertThat(StoreOptionDelta.isHidden(null)).isFalse();
    }

    @Test
    void isHidden_whenOverride_returnsFalse() {
        StoreOptionDelta delta = new StoreOptionDelta(null, null);
        delta.override(BigDecimal.valueOf(3.49), DeltaType.OVERRIDE);

        assertThat(StoreOptionDelta.isHidden(delta)).isFalse();
    }

    @Test
    void isHidden_whenHidden_returnsTrue() {
        StoreOptionDelta delta = new StoreOptionDelta(null, null);
        delta.override(null, DeltaType.HIDDEN);

        assertThat(StoreOptionDelta.isHidden(delta)).isTrue();
    }

    @Test
    void resolveBaseExtraPrice_whenDeltaIsNull_returnsProductOptionPrice() {
        ProductOption productOption = productOptionWithPrice(BigDecimal.valueOf(1.99));

        BigDecimal result = StoreOptionDelta.resolveBaseExtraPrice(productOption, null);

        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(1.99));
    }

    @Test
    void resolveBaseExtraPrice_whenNoneCountTypeWithNullPrice_returnsNull() {
        ProductOption productOption = ProductOption.builder()
                .id(1L)
                .extraPrice(null)
                .countType(CountType.NONE)
                .build();

        BigDecimal result = StoreOptionDelta.resolveBaseExtraPrice(productOption, null);

        assertThat(result).isNull();
    }

    @Test
    void resolveBaseExtraPrice_whenOverride_returnsOverridePrice() {
        ProductOption productOption = productOptionWithPrice(BigDecimal.valueOf(1.99));
        StoreOptionDelta delta = new StoreOptionDelta(productOption, null);
        delta.override(BigDecimal.valueOf(3.49), DeltaType.OVERRIDE);

        BigDecimal result = StoreOptionDelta.resolveBaseExtraPrice(productOption, delta);

        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(3.49));
    }

    @Test
    void resolveBaseExtraPrice_whenHidden_throws() {
        ProductOption productOption = productOptionWithPrice(BigDecimal.valueOf(1.99));
        StoreOptionDelta delta = new StoreOptionDelta(productOption, null);
        delta.override(null, DeltaType.HIDDEN);

        assertThatThrownBy(() -> StoreOptionDelta.resolveBaseExtraPrice(productOption, delta))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private ProductOption productOptionWithPrice(BigDecimal extraPrice) {
        return ProductOption.builder()
                .id(1L)
                .extraPrice(extraPrice)
                .countType(CountType.COUNTABLE)
                .build();
    }
}
