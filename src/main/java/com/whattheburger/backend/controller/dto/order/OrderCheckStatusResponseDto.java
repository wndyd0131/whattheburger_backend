package com.whattheburger.backend.controller.dto.order;

import com.whattheburger.backend.domain.enums.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class OrderCheckStatusResponseDto {
    private UUID orderNumber;
    private BigDecimal totalPrice;
    private OrderStatus orderStatus;
    private OrderType orderType;
    private PaymentStatus paymentStatus;
}
