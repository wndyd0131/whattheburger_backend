package com.whattheburger.backend.controller.dto;

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
    private Boolean couponApplied;
    private Double discountPrice;
    private List<ProductRequest> productRequests;
    // Store

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class ProductRequest {
        private Long productId;
        private Integer quantity;
        private String forWhom;
        private List<ProductOptionRequest> productOptionRequests;
    }

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class ProductOptionRequest {
        private Long productOptionId;
        private Integer quantity;
        private List<ProductOptionTraitRequest> productOptionTraitRequests;
    }

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class ProductOptionTraitRequest {
        private Long productOptionTraitId;
        private Integer value;
    }
}