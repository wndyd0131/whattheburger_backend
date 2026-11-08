package com.whattheburger.backend.loadtest;

import com.whattheburger.backend.domain.Store;
import com.whattheburger.backend.domain.enums.OrderStatus;
import com.whattheburger.backend.domain.enums.OrderType;
import com.whattheburger.backend.domain.enums.PaymentStatus;
import com.whattheburger.backend.domain.enums.ProductType;
import com.whattheburger.backend.domain.order.Order;
import com.whattheburger.backend.domain.order.OrderCustomRule;
import com.whattheburger.backend.domain.order.OrderProduct;
import com.whattheburger.backend.domain.order.OrderProductOption;
import com.whattheburger.backend.domain.order.QuantityDetail;
import com.whattheburger.backend.loadtest.dto.LoadTestDeductStockRequest;
import com.whattheburger.backend.loadtest.dto.LoadTestDeductStockRequest.OrderCustomRulePayload;
import com.whattheburger.backend.loadtest.dto.LoadTestDeductStockRequest.OrderProductOptionPayload;
import com.whattheburger.backend.loadtest.dto.LoadTestDeductStockRequest.OrderProductPayload;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class LoadTestOrderAssembler {

    public Order assemble(Store store, LoadTestDeductStockRequest request) {
        Order order = Order.builder()
                .store(store)
                .orderType(OrderType.DELIVERY)
                .orderStatus(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PAID)
                .totalPrice(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .build();

        List<OrderProduct> orderProducts = request.getOrderProducts().stream()
                .map(payload -> toOrderProduct(payload, order))
                .toList();
        order.assignOrderProducts(new ArrayList<>(orderProducts));
        return order;
    }

    private OrderProduct toOrderProduct(OrderProductPayload payload, Order order) {
        OrderProduct orderProduct = OrderProduct.builder()
                .storeProductId(payload.getStoreProductId())
                .quantity(Optional.ofNullable(payload.getQuantity()).orElse(1))
                .name("loadtest")
                .totalPrice(BigDecimal.ZERO)
                .extraPrice(BigDecimal.ZERO)
                .basePrice(BigDecimal.ZERO)
                .totalCalories(0.0)
                .productType(ProductType.ONLY)
                .orderCustomRules(new ArrayList<>())
                .build();

        List<OrderCustomRule> rules = payload.getOrderCustomRules().stream()
                .map(rulePayload -> toOrderCustomRule(rulePayload, orderProduct))
                .toList();
        orderProduct.assignOrderCustomRules(new ArrayList<>(rules));
        orderProduct.assignOrder(order);
        return orderProduct;
    }

    private OrderCustomRule toOrderCustomRule(OrderCustomRulePayload payload, OrderProduct orderProduct) {
        OrderCustomRule rule = OrderCustomRule.builder()
                .customRuleId(payload.getCustomRuleId())
                .name("loadtest-rule")
                .totalPrice(BigDecimal.ZERO)
                .orderProductOptions(new ArrayList<>())
                .build();

        List<OrderProductOption> options = payload.getOrderProductOptions().stream()
                .map(optionPayload -> toOrderProductOption(optionPayload, rule))
                .toList();
        rule.assignOrderProductOptions(new ArrayList<>(options));
        rule.assignOrderProduct(orderProduct);
        return rule;
    }

    private OrderProductOption toOrderProductOption(
            OrderProductOptionPayload payload,
            OrderCustomRule orderCustomRule
    ) {
        QuantityDetail quantityDetail = Optional.ofNullable(payload.getQuantityDetail())
                .map(qd -> new QuantityDetail(
                        qd.getProductOptionOptionQuantityId(),
                        qd.getQuantityType(),
                        BigDecimal.ZERO,
                        0.0
                ))
                .orElse(null);

        OrderProductOption option = OrderProductOption.builder()
                .productOptionId(payload.getProductOptionId())
                .name("loadtest-option")
                .countType(payload.getCountType())
                .totalPrice(BigDecimal.ZERO)
                .basePrice(BigDecimal.ZERO)
                .totalCalories(0.0)
                .quantity(payload.getQuantity())
                .quantityDetail(quantityDetail)
                .orderProductOptionTraits(new ArrayList<>())
                .build();
        option.assignOrderCustomRule(orderCustomRule);
        return option;
    }
}
