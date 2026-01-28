package com.whattheburger.backend.controller.dto.order;

import com.whattheburger.backend.domain.enums.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class OrderDto {
    private Long id;
    private UUID orderNumber;
    private Long storeId;
    private Instant createdAt;
    private Instant updatedAt;
    private BigDecimal totalPrice;
    private OrderStatus orderStatus;
    private OrderType orderType;
}
