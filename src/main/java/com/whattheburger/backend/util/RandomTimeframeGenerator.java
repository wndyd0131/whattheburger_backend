package com.whattheburger.backend.util;

import com.whattheburger.backend.domain.enums.OrderStatus;

public class RandomTimeframeGenerator {
    public Integer generate(OrderStatus orderStatus) {
        switch(orderStatus) {
            case CONFIRMING -> {
                return null;
            }
            default -> {
                return null;
            }
        }
    }
}
