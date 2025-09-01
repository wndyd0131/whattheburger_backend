package com.whattheburger.backend.controller.dto_mapper;

import com.whattheburger.backend.controller.dto.order.*;
import com.whattheburger.backend.controller.dto.order.QuantityDetail;
import com.whattheburger.backend.domain.order.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class OrderSessionResponseDtoMapper {
    public OrderSessionResponseDto toOrderSessionResponseDto(OrderSession orderSession) {
        return OrderSessionResponseDto
                .builder()
                .orderType(orderSession.getOrderType())
                .taxAmount(orderSession.getTaxAmount())
                .totalPrice(orderSession.getTotalPrice())
                .productResponses(orderSession.getOrderSessionProducts().stream()
                        .map(this::toProductResponseDto).toList())
                .build();
    }

    private OrderSessionProductResponseDto toProductResponseDto(OrderSessionProduct sessionProduct) {
        log.info("Product ID {}", sessionProduct.getProductId());
        OrderSessionProductResponseDto productResponseDto = OrderSessionProductResponseDto
                .builder()
                .productId(sessionProduct.getProductId())
                .quantity(sessionProduct.getQuantity())
                .name(sessionProduct.getName())
                .totalPrice(sessionProduct.getTotalPrice())
                .extraPrice(sessionProduct.getExtraPrice())
                .basePrice(sessionProduct.getBasePrice())
                .calculatedCalories(sessionProduct.getTotalCalories())
                .imageSource(sessionProduct.getImageSource())
                .productType(sessionProduct.getProductType())
                .customRuleResponses(sessionProduct.getOrderSessionCustomRules().stream()
                        .map(this::toCustomRuleResponseDto).toList())
                .build();
        return productResponseDto;
    }

    private OrderSessionCustomRuleResponseDto toCustomRuleResponseDto(OrderSessionCustomRule sessionCustomRule) {
        OrderSessionCustomRuleResponseDto customRuleResponseDto = OrderSessionCustomRuleResponseDto
                .builder()
                .customRuleId(sessionCustomRule.getCustomRuleId())
                .name(sessionCustomRule.getName())
                .calculatedPrice(sessionCustomRule.getTotalPrice())
                .optionResponses(sessionCustomRule.getOrderSessionOptions().stream()
                        .map(this::toOptionResponseDto).toList())
                .build();
        return customRuleResponseDto;
    }
    private OrderSessionOptionResponseDto toOptionResponseDto(OrderSessionOption sessionOption) {
        QuantityDetail quantityDetailResponse = Optional.ofNullable(sessionOption.getQuantityDetail())
                .map(quantityDetail -> new QuantityDetail(
                        quantityDetail.getProductOptionOptionQuantityId(),
                        quantityDetail.getQuantityType(),
                        quantityDetail.getQuantityExtraPrice(),
                        quantityDetail.getQuantityExtraCalories())
                ).orElse(null);
        OrderSessionOptionResponseDto optionResponseDto = OrderSessionOptionResponseDto
                .builder()
                .name(sessionOption.getName())
                .productOptionId(sessionOption.getProductOptionId())
                .countType(sessionOption.getCountType())
                .calculatedPrice(sessionOption.getTotalPrice())
                .basePrice(sessionOption.getBasePrice())
                .calculatedCalories(sessionOption.getTotalCalories())
                .quantity(sessionOption.getQuantity())
                .quantityDetail(quantityDetailResponse)
                .traitResponses(sessionOption.getOrderSessionOptionTraits().stream()
                        .map(this::toTraitResponseDto).toList())
                .build();
        return optionResponseDto;
    }
    private OrderSessionTraitResponseDto toTraitResponseDto(OrderSessionOptionTrait sessionOptionTrait) {
        OrderSessionTraitResponseDto traitResponseDto = OrderSessionTraitResponseDto
                .builder()
                .name(sessionOptionTrait.getName())
                .productOptionTraitId(sessionOptionTrait.getProductOptionTraitId())
                .labelCode(sessionOptionTrait.getLabelCode())
                .calculatedPrice(sessionOptionTrait.getTotalPrice())
                .basePrice(sessionOptionTrait.getBasePrice())
                .calculatedCalories(sessionOptionTrait.getCalculatedCalories())
                .optionTraitType(sessionOptionTrait.getOptionTraitType())
                .selectedValue(sessionOptionTrait.getSelectedValue())
                .build();
        return traitResponseDto;
    }
}
