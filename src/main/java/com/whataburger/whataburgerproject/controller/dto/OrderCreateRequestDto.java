package com.whataburger.whataburgerproject.controller.dto;

import com.whataburger.whataburgerproject.domain.enums.OrderStatus;
import com.whataburger.whataburgerproject.domain.enums.OrderType;
import com.whataburger.whataburgerproject.domain.enums.PaymentMethod;
import com.whataburger.whataburgerproject.domain.enums.PaymentStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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