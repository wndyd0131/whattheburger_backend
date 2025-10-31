package com.whattheburger.backend.controller.dto_mapper;

import com.whattheburger.backend.controller.dto.order.*;
import com.whattheburger.backend.controller.dto.order.QuantityDetail;
import com.whattheburger.backend.domain.order.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class OrderSessionResponseDtoMapper {
    @Value("${aws.s3-bucket-name}")
    private String s3bucketName;
    @Value("${aws.s3-url-postfix}")
    private String s3urlPostfix;

    public OrderSessionResponseDto toOrderSessionResponseDto(OrderSession orderSession) {
        return OrderSessionResponseDto
                .builder()
                .sessionId(orderSession.getSessionId())
                .storeId(orderSession.getStoreId())
                .orderType(orderSession.getOrderType())
                .taxAmount(orderSession.getTaxAmount())
                .totalPrice(orderSession.getTotalPrice())
                .productResponses(orderSession.getOrderSessionProducts().stream()
                        .map(this::toProductResponseDto).toList())
                .build();
    }

    private OrderSessionProductResponseDto toProductResponseDto(OrderSessionProduct sessionProduct) {
        log.info("storeProduct ID {}", sessionProduct.getStoreProductId());
        String publicUrl = "https://" + s3bucketName + "." + s3urlPostfix + "/";
        String productImageUrl = Optional.ofNullable(sessionProduct.getImageSource())
                .map(imageSource -> publicUrl + imageSource)
                .orElse(null);
        OrderSessionProductResponseDto productResponseDto = OrderSessionProductResponseDto
                .builder()
                .storeProductId(sessionProduct.getStoreProductId())
                .quantity(sessionProduct.getQuantity())
                .name(sessionProduct.getName())
                .totalPrice(sessionProduct.getTotalPrice())
                .extraPrice(sessionProduct.getExtraPrice())
                .basePrice(sessionProduct.getBasePrice())
                .calculatedCalories(sessionProduct.getTotalCalories())
                .imageSource(productImageUrl)
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
