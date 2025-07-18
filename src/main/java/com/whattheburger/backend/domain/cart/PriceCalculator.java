package com.whattheburger.backend.domain.cart;

public interface PriceCalculator<T> {
    Double calculateTotalPrice(T detail);
}
