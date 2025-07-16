package com.whattheburger.backend.utils;

import com.whattheburger.backend.controller.dto.order.OrderCreateRequestDto;
import com.whattheburger.backend.controller.dto.order.ProductOptionRequest;
import com.whattheburger.backend.controller.dto.order.ProductOptionTraitRequest;
import com.whattheburger.backend.controller.dto.order.ProductRequest;
import com.whattheburger.backend.domain.enums.OrderType;
import com.whattheburger.backend.domain.enums.PaymentMethod;

import java.util.List;

public class MockOrderRequestFactory {
    public static OrderCreateRequestDto createOrderCreateRequest() {
        return OrderCreateRequestDto
                .builder()
                .orderType(OrderType.DELIVERY)
                .orderNote("order note")
                .paymentMethod(PaymentMethod.CASH)
                .totalPrice(0D)
                .productRequests(
                        List.of(
                                ProductRequest
                                        .builder()
                                        .productId(1L)
                                        .quantity(1)
                                        .forWhom("")
                                        .productOptionRequests(
                                                List.of(
                                                        ProductOptionRequest
                                                                .builder()
                                                                .productOptionId(1L)
                                                                .quantity(1)
                                                                .productOptionTraitRequests(
                                                                        List.of(ProductOptionTraitRequest
                                                                                .builder()
                                                                                .productOptionTraitId(1L)
                                                                                .value(0)
                                                                                .build()
                                                                        )
                                                                )
                                                                .build()
                                                )
                                        )
                                        .build()
                        )

                )
                .build();
    }
}
