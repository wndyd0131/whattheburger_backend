package com.whattheburger.backend.service;

import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.domain.enums.OrderStatus;
import com.whattheburger.backend.domain.enums.OrderType;
import com.whattheburger.backend.domain.enums.PaymentStatus;
import com.whattheburger.backend.domain.order.*;
import com.whattheburger.backend.domain.order.QuantityDetail;
import com.whattheburger.backend.service.dto.cart.*;
import com.whattheburger.backend.service.dto.cart.calculator.ProcessedOptionDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class OrderFactory {
    public Order createFromCartDto(ProcessedCartDto cartDto, OrderType orderType) {
        Order order = Order
                .builder()
                .orderStatus(OrderStatus.PENDING)
                .orderType(orderType)
                .paymentStatus(PaymentStatus.UNPAID)
                .totalPrice(cartDto.getTotalPrice())
                .orderProducts(new ArrayList<>())
                .build();
        List<ProcessedProductDto> productDtos = cartDto.getProcessedProductDtos();
        List<OrderProduct> orderProducts = productDtos.stream()
                .map(productDto -> createOrderProduct(productDto, order))
                .toList();
        order.assignOrderProducts(orderProducts);
        return order;
    }

    public Order overWriteOrder(ProcessedCartDto cartDto, OrderType orderType, Order order) {
        // Order set anything
        List<ProcessedProductDto> productDtos = cartDto.getProcessedProductDtos();
        List<OrderProduct> orderProducts = productDtos.stream()
                .map(productDto -> createOrderProduct(productDto, order))
                .toList();
        order.assignOrderProducts(orderProducts);
        return order;
    }

    private OrderProduct createOrderProduct(ProcessedProductDto productDto, Order order) {
        Product product = productDto.getProduct();
        log.info("Product ID {}", product.getId());
        OrderProduct orderProduct = OrderProduct
                .builder()
                .productId(product.getId())
                .quantity(productDto.getQuantity())
                .name(product.getName())
                .calculatedPrice(productDto.getCalculatedProductPrice())
                .imageSource(product.getImageSource())
                .calculatedCalories(product.getCalories())
                .productType(product.getProductType())
                .orderCustomRules(new ArrayList<>())
                .build();
        List<OrderCustomRule> orderCustomRules = productDto.getProcessedCustomRuleDtos().stream()
                .map(customRuleDto -> createOrderCustomRule(customRuleDto, orderProduct))
                .toList();
        orderProduct.assignOrderCustomRules(orderCustomRules);
        orderProduct.assignOrder(order);

        return orderProduct;
    }

    private OrderCustomRule createOrderCustomRule(ProcessedCustomRuleDto customRuleDto, OrderProduct orderProduct) {
        CustomRule customRule = customRuleDto.getCustomRule();
        OrderCustomRule orderCustomRule = OrderCustomRule
                .builder()
                .customRuleId(customRule.getId())
                .calculatedPrice(customRuleDto.getCalculatedCustomRulePrice())
                .name(customRule.getName())
                .orderProductOptions(new ArrayList<>())
                .build();
        List<OrderProductOption> orderProductOptions = customRuleDto.getProcessedOptionDtos().stream()
                .filter(optionDto -> optionDto.getIsSelected())
                .map(optionDto -> createOrderProductOption(optionDto, orderCustomRule))
                .toList();
        orderCustomRule.assignOrderProductOptions(orderProductOptions);
        orderCustomRule.assignOrderProduct(orderProduct);

        return orderCustomRule;
    }

    private OrderProductOption createOrderProductOption(ProcessedOptionDto optionDto, OrderCustomRule orderCustomRule) {
        QuantityDetail quantityDetail = Optional.ofNullable(optionDto.getProcessedQuantityDto())
                .map(processedQuantityDto -> {
                    ProductOptionOptionQuantity selectedQuantity = processedQuantityDto.getSelectedQuantity();
                    return new QuantityDetail(
                            selectedQuantity.getId(),
                            selectedQuantity.getOptionQuantity().getQuantity().getQuantityType(),
                            selectedQuantity.getExtraPrice(),
                            selectedQuantity.getOptionQuantity().getExtraCalories()
                    );
                }).orElse(null);

        ProductOption productOption = optionDto.getProductOption();
        OrderProductOption orderProductOption = OrderProductOption
                .builder()
                .productOptionId(productOption.getId())
                .name(productOption.getOption().getName())
                .countType(productOption.getCountType())
                .calculatedPrice(optionDto.getCalculatedOptionPrice())
                .calculatedCalories(productOption.getOption().getCalories())
                .quantity(optionDto.getQuantity())
                .quantityDetail(quantityDetail)
                .orderProductOptionTraits(new ArrayList<>())
                .build();
        List<OrderProductOptionTrait> orderProductOptionTraits = optionDto.getProcessedTraitDtos().stream()
                .map(traitDto -> createOrderProductOptionTrait(traitDto, orderProductOption))
                .toList();
        log.info("OPOT {}", orderProductOptionTraits);
        orderProductOption.assignOrderProductOptionTraits(orderProductOptionTraits);
        orderProductOption.assignOrderCustomRule(orderCustomRule);
        return orderProductOption;
    }

    private OrderProductOptionTrait createOrderProductOptionTrait(ProcessedTraitDto traitDto, OrderProductOption orderProductOption) {
        ProductOptionTrait productOptionTrait = traitDto.getProductOptionTrait();
        OrderProductOptionTrait orderProductOptionTrait = OrderProductOptionTrait
                .builder()
                .productOptionTraitId(productOptionTrait.getId())
                .name(productOptionTrait.getOptionTrait().getName())
                .optionTraitType(productOptionTrait.getOptionTrait().getOptionTraitType())
                .calculatedCalories(productOptionTrait.getExtraCalories())
                .calculatedPrice(traitDto.getCalculatedTraitPrice())
                .labelCode(productOptionTrait.getOptionTrait().getLabelCode())
                .selectedValue(traitDto.getCurrentValue())
                .build();
        orderProductOptionTrait.assignOrderProductOption(orderProductOption);
        return orderProductOptionTrait;
    }
}
