package com.whattheburger.backend.controller.dto_mapper;

import com.whattheburger.backend.controller.dto.order.*;
import com.whattheburger.backend.controller.dto.order.QuantityDetail;
import com.whattheburger.backend.domain.enums.CountType;
import com.whattheburger.backend.domain.enums.OptionTraitType;
import com.whattheburger.backend.domain.enums.ProductType;
import com.whattheburger.backend.domain.order.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
@Slf4j
public class OrderResponseDtoMapper {
    public OrderPreviewResponseDto toOrderResponseDto(Order order) {
        OrderPreviewResponseDto orderPreviewResponseDto = OrderPreviewResponseDto
                .builder()
                .id(order.getId())
                .orderType(order.getOrderType())
                .taxAmount(order.getTaxAmount())
                .totalPrice(order.getTotalPrice())
                .productResponseDtos(order.getOrderProducts().stream()
                        .map(this::toProductResponseDto).toList())
                .build();
        return orderPreviewResponseDto;
    }

    private ProductResponseDto toProductResponseDto(OrderProduct orderProduct) {
        log.info("Product ID {}", orderProduct.getProductId());
        ProductResponseDto productResponseDto = ProductResponseDto
                .builder()
                .id(orderProduct.getId())
                .productId(orderProduct.getProductId())
                .quantity(orderProduct.getQuantity())
                .name(orderProduct.getName())
                .calculatedPrice(orderProduct.getCalculatedPrice())
                .calculatedCalories(orderProduct.getCalculatedCalories())
                .imageSource(orderProduct.getImageSource())
                .productType(orderProduct.getProductType())
                .customRuleResponseDtos(orderProduct.getOrderCustomRules().stream()
                        .map(this::toCustomRuleResponseDto).toList())
                .build();
        return productResponseDto;
    }

    private CustomRuleResponseDto toCustomRuleResponseDto(OrderCustomRule orderCustomRule) {
        CustomRuleResponseDto customRuleResponseDto = CustomRuleResponseDto
                .builder()
                .id(orderCustomRule.getId())
                .customRuleId(orderCustomRule.getCustomRuleId())
                .name(orderCustomRule.getName())
                .calculatedPrice(orderCustomRule.getCalculatedPrice())
                .optionResponseDtos(orderCustomRule.getOrderProductOptions().stream()
                        .map(this::toOptionResponseDto).toList())
                .build();
        return customRuleResponseDto;
    }
    private OptionResponseDto toOptionResponseDto(OrderProductOption orderProductOption) {
        QuantityDetail quantityDetailResponse = Optional.ofNullable(orderProductOption.getQuantityDetail())
                .map(quantityDetail -> new QuantityDetail(
                        quantityDetail.getProductOptionOptionQuantityId(),
                        quantityDetail.getQuantityType(),
                        quantityDetail.getQuantityExtraPrice(),
                        quantityDetail.getQuantityExtraCalories())
                ).orElse(null);
        OptionResponseDto optionResponseDto = OptionResponseDto
                .builder()
                .id(orderProductOption.getId())
                .name(orderProductOption.getName())
                .productOptionId(orderProductOption.getProductOptionId())
                .countType(orderProductOption.getCountType())
                .calculatedPrice(orderProductOption.getCalculatedPrice())
                .calculatedCalories(orderProductOption.getCalculatedCalories())
                .quantity(orderProductOption.getQuantity())
                .quantityDetail(quantityDetailResponse)
                .traitResponseDtos(orderProductOption.getOrderProductOptionTraits().stream()
                        .map(this::toTraitResponseDto).toList())
                .build();
        return optionResponseDto;
    }
    private TraitResponseDto toTraitResponseDto(OrderProductOptionTrait orderProductOptionTrait) {
        TraitResponseDto traitResponseDto = TraitResponseDto
                .builder()
                .id(orderProductOptionTrait.getId())
                .name(orderProductOptionTrait.getName())
                .productOptionTraitId(orderProductOptionTrait.getProductOptionTraitId())
                .labelCode(orderProductOptionTrait.getLabelCode())
                .calculatedPrice(orderProductOptionTrait.getCalculatedPrice())
                .calculatedCalories(orderProductOptionTrait.getCalculatedCalories())
                .optionTraitType(orderProductOptionTrait.getOptionTraitType())
                .selectedValue(orderProductOptionTrait.getSelectedValue())
                .build();
        return traitResponseDto;
    }
}
