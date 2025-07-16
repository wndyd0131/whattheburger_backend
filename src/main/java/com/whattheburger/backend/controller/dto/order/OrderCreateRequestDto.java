package com.whattheburger.backend.controller.dto.order;

import com.whattheburger.backend.domain.DiscountPolicy;
import com.whattheburger.backend.domain.enums.OrderType;
import com.whattheburger.backend.domain.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class OrderCreateRequestDto {
    private OrderType orderType;
    private String orderNote;
    private PaymentMethod paymentMethod;
    private Double totalPrice;
    private List<ProductRequest> productRequests;
    // Coupon
    // Store
}