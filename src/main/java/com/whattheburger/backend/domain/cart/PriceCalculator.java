package com.whattheburger.backend.domain.cart;

import java.math.BigDecimal;

public interface PriceCalculator<T> {
    BigDecimal calculateTotalPrice(T detail);
}
