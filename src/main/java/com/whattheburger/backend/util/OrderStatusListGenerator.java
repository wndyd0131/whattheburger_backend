package com.whattheburger.backend.util;

import com.whattheburger.backend.domain.enums.OrderStatus;
import com.whattheburger.backend.domain.enums.OrderType;

import java.util.List;

public class OrderStatusListGenerator {
    public List<OrderStatus> generate(OrderType orderType) {
        switch(orderType) {
            case DELIVERY: {
                return List.of(
                        OrderStatus.PENDING,
                        OrderStatus.CONFIRMING,
                        OrderStatus.PREPARING,
                        OrderStatus.DELIVERING,
                        OrderStatus.COMPLETED
                );
            }
            case PICK_UP: {
                return List.of(

                );
            }
            default:
                return null;
        }
    }
}
