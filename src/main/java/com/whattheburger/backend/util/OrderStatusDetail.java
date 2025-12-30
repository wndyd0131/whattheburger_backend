package com.whattheburger.backend.util;

import com.whattheburger.backend.domain.enums.OrderStatus;

public record OrderStatusDetail (
        Integer time,
        OrderStatus next
) {}
