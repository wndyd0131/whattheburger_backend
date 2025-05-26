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
    private Double totalPrice;
    private List<ProductRequest> productRequests;
    private OrderType orderType;
    private PaymentMethod paymentMethod;
    private String orderNote;

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class ProductRequest {
        private Long productId;
        private Integer quantity;
        private String forWhom;
        private List<OptionRequest> optionRequests;
    }

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class OptionRequest {
        private Long optionId;
        private List<OptionTraitRequest> optionTraitRequests;
    }

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class OptionTraitRequest {
        private Long optionTraitId;
    }
}