package com.whattheburger.backend.controller.dto.order;

import com.whattheburger.backend.domain.enums.*;
import com.whattheburger.backend.domain.order.GuestInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class OrderSessionResponseDto {
    private UUID sessionId;
    private Long storeId;
    private BigDecimal totalPrice;
    private OrderStatus orderStatus;
    private OrderType orderType;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private String orderNote;
    private DiscountType discountType;
    private BigDecimal taxAmount;
    private GuestInfo guestInfo;
    private List<OrderSessionProductResponseDto> productResponses;
}
