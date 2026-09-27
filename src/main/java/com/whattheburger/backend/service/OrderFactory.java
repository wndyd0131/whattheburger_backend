package com.whattheburger.backend.service;

import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.domain.checkout.CheckoutAttempt;
import com.whattheburger.backend.domain.enums.*;
import com.whattheburger.backend.domain.order.*;
import com.whattheburger.backend.domain.order.QuantityDetail;
import com.whattheburger.backend.service.dto.cart.*;
import com.whattheburger.backend.service.dto.cart.calculator.ProcessedOptionDto;
import jakarta.persistence.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
@Slf4j
public class OrderFactory {

    public Order createFromOrderSession(OrderSession orderSession, User user, Store store) {
        Order.OrderBuilder orderBuilder = Order.builder()
                .orderType(orderSession.getOrderType())
                .store(store)
                .orderStatus(orderSession.getOrderStatus())
                .orderNote(orderSession.getOrderNote())
                .paymentStatus(orderSession.getPaymentStatus())
                .discountType(orderSession.getDiscountType())
                .taxAmount(orderSession.getTaxAmount())
                .totalPrice(orderSession.getTotalPrice())
                .user(user)
                .contactInfo(orderSession.getContactInfo());

        if (orderSession.getOrderType() == OrderType.DELIVERY) {
            orderBuilder
                    .addressInfo(orderSession.getAddressInfo());
        } else if (orderSession.getOrderType() == OrderType.PICK_UP) {
            Order.builder()
                    .pickupInfo(orderSession.getPickupInfo());
        } else {
            throw new IllegalStateException();
        }
        Order order = orderBuilder.build();
        List<OrderProduct> orderProducts = orderSession.getOrderSessionProducts().stream()
                .map(sessionProduct -> createOrderProduct(sessionProduct, order))
                .toList();
        order.assignOrderProducts(orderProducts);
        return order;
    }

    public Order createFromCheckoutAttempt(CheckoutAttempt checkoutAttempt, User user, Store store) {
        Order.OrderBuilder orderBuilder = Order.builder()
                .orderType(checkoutAttempt.getOrderType())
                .store(store)
                .orderStatus(OrderStatus.PENDING)
                .orderNote(checkoutAttempt.getOrderNote())
                .paymentStatus(checkoutAttempt.getPaymentStatus())
                .discountType(checkoutAttempt.getDiscountType())
                .taxAmount(checkoutAttempt.getTaxAmount())
                .totalPrice(checkoutAttempt.getTotalPrice())
                .contactInfo(checkoutAttempt.getContactInfo());

        if (user != null) {
            orderBuilder.user(user);
        } else {
            orderBuilder.guestInfo(new GuestInfo(checkoutAttempt.getGuestId()));
        }

        if (checkoutAttempt.getOrderType() == OrderType.DELIVERY) {
            orderBuilder.addressInfo(checkoutAttempt.getAddressInfo());
        } else if (checkoutAttempt.getOrderType() == OrderType.PICK_UP) {
            throw new IllegalStateException("Pickup checkout attempt is not supported yet");
        } else {
            throw new IllegalStateException();
        }

        Order order = orderBuilder.build();
        List<OrderProduct> orderProducts = checkoutAttempt.getOrderRecord().stream()
                .map(sessionProduct -> createOrderProduct(sessionProduct, order))
                .toList();
        order.assignOrderProducts(orderProducts);
        return order;
    }

    private OrderProduct createOrderProduct(OrderSessionProduct sessionProduct, Order order) {
        log.info("Product ID {}", sessionProduct.getStoreProductId());
        OrderProduct orderProduct = OrderProduct
                .builder()
                .storeProductId(sessionProduct.getStoreProductId())
                .quantity(sessionProduct.getQuantity())
                .name(sessionProduct.getName())
                .totalPrice(sessionProduct.getTotalPrice())
                .extraPrice(sessionProduct.getExtraPrice())
                .basePrice(sessionProduct.getBasePrice())
                .imageSource(sessionProduct.getImageSource())
                .totalCalories(sessionProduct.getTotalCalories())
                .productType(sessionProduct.getProductType())
                .orderCustomRules(new ArrayList<>())
                .build();
        List<OrderCustomRule> orderCustomRules = Optional.ofNullable(sessionProduct.getOrderSessionCustomRules())
                .orElse(List.of())
                .stream()
                .map(sessionCustomRule -> createOrderCustomRule(sessionCustomRule, orderProduct))
                .toList();
        orderProduct.assignOrderCustomRules(orderCustomRules);
        orderProduct.assignOrder(order);

        return orderProduct;
    }

    private OrderCustomRule createOrderCustomRule(OrderSessionCustomRule sessionCustomRule, OrderProduct orderProduct) {
        OrderCustomRule orderCustomRule = OrderCustomRule
                .builder()
                .customRuleId(sessionCustomRule.getCustomRuleId())
                .totalPrice(sessionCustomRule.getTotalPrice())
                .name(sessionCustomRule.getName())
                .orderProductOptions(new ArrayList<>())
                .build();
        List<OrderProductOption> orderProductOptions = sessionCustomRule.getOrderSessionOptions().stream()
                .map(sessionOption -> createOrderProductOption(sessionOption, orderCustomRule))
                .toList();
        orderCustomRule.assignOrderProductOptions(orderProductOptions);
        orderCustomRule.assignOrderProduct(orderProduct);

        return orderCustomRule;
    }

    private OrderProductOption createOrderProductOption(OrderSessionOption sessionOption, OrderCustomRule orderCustomRule) {
        QuantityDetail quantityDetail = Optional.ofNullable(sessionOption.getQuantityDetail())
                .map(sessionQuantityDetail ->
                    new QuantityDetail(
                            sessionQuantityDetail.getProductOptionOptionQuantityId(),
                            sessionQuantityDetail.getQuantityType(),
                            sessionQuantityDetail.getQuantityExtraPrice(),
                            sessionQuantityDetail.getQuantityExtraCalories()
                    )
                ).orElse(null);

        OrderProductOption orderProductOption = OrderProductOption
                .builder()
                .productOptionId(sessionOption.getProductOptionId())
                .name(sessionOption.getName())
                .countType(sessionOption.getCountType())
                .totalPrice(sessionOption.getTotalPrice())
                .basePrice(sessionOption.getBasePrice())
                .totalCalories(sessionOption.getTotalCalories())
                .quantity(sessionOption.getQuantity())
                .quantityDetail(quantityDetail)
                .orderProductOptionTraits(new ArrayList<>())
                .build();
        List<OrderProductOptionTrait> orderProductOptionTraits = Optional.ofNullable(sessionOption.getOrderSessionOptionTraits())
                .orElse(List.of())
                .stream()
                .map(sessionOptionTrait -> createOrderProductOptionTrait(sessionOptionTrait, orderProductOption))
                .toList();
        log.info("OPOT {}", orderProductOptionTraits);
        orderProductOption.assignOrderProductOptionTraits(orderProductOptionTraits);
        orderProductOption.assignOrderCustomRule(orderCustomRule);
        return orderProductOption;
    }

    private OrderProductOptionTrait createOrderProductOptionTrait(OrderSessionOptionTrait sessionOptionTrait, OrderProductOption orderProductOption) {
        OrderProductOptionTrait orderProductOptionTrait = OrderProductOptionTrait
                .builder()
                .productOptionTraitId(sessionOptionTrait.getProductOptionTraitId())
                .name(sessionOptionTrait.getName())
                .optionTraitType(sessionOptionTrait.getOptionTraitType())
                .calculatedCalories(sessionOptionTrait.getCalculatedCalories())
                .totalPrice(sessionOptionTrait.getTotalPrice())
                .basePrice(sessionOptionTrait.getBasePrice())
                .labelCode(sessionOptionTrait.getLabelCode())
                .selectedValue(sessionOptionTrait.getSelectedValue())
                .build();
        orderProductOptionTrait.assignOrderProductOption(orderProductOption);
        return orderProductOptionTrait;
    }
}
