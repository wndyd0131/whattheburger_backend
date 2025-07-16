package com.whattheburger.backend.domain;

public interface DiscountPolicy {
    boolean isApplicable();
    double apply();
}
