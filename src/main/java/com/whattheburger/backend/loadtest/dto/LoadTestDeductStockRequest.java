package com.whattheburger.backend.loadtest.dto;

import com.whattheburger.backend.domain.enums.CountType;
import com.whattheburger.backend.domain.enums.QuantityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoadTestDeductStockRequest {
    @Builder.Default
    private List<OrderProductPayload> orderProducts = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderProductPayload {
        private Long storeProductId;
        private Integer quantity;
        @Builder.Default
        private List<OrderCustomRulePayload> orderCustomRules = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderCustomRulePayload {
        private Long customRuleId;
        @Builder.Default
        private List<OrderProductOptionPayload> orderProductOptions = new ArrayList<>();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderProductOptionPayload {
        private Long productOptionId;
        private CountType countType;
        private Integer quantity;
        private QuantityDetailPayload quantityDetail;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuantityDetailPayload {
        private Long productOptionOptionQuantityId;
        private QuantityType quantityType;
    }
}
