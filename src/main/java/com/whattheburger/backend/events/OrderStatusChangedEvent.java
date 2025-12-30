package com.whattheburger.backend.events;

import com.whattheburger.backend.domain.enums.OrderStatus;
import com.whattheburger.backend.domain.order.Order;
import com.whattheburger.backend.domain.order.OrderSession;

import java.util.UUID;

public record OrderStatusChangedEvent (
        UUID orderSessionId,
        OrderStatus orderStatus
) {}
