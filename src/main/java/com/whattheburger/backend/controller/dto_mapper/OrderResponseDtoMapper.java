package com.whattheburger.backend.controller.dto_mapper;

import com.whattheburger.backend.controller.dto.order.*;
import com.whattheburger.backend.controller.dto.order.QuantityDetail;
import com.whattheburger.backend.domain.enums.*;
import com.whattheburger.backend.domain.order.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


@Component
@Slf4j
public class OrderResponseDtoMapper {
    @Value("${aws.s3-bucket-name}")
    private String s3bucketName;
    @Value("${aws.s3-url-postfix}")
    private String s3urlPostfix;
    public OrderResponseDto toOrderResponseDto(Order order) {
        OrderResponseDto orderResponseDto = OrderResponseDto
                .builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .storeId(order.getStore().getId())
                .totalPrice(order.getTotalPrice())
                .orderStatus(order.getOrderStatus())
                .orderType(order.getOrderType())
                .paymentStatus(order.getPaymentStatus())
                .paymentMethod(order.getPaymentMethod())
                .orderNote(order.getOrderNote())
                .discountType(order.getDiscountType())
                .taxAmount(order.getTaxAmount())
                .guestInfo(order.getGuestInfo())
                .contactInfo(order.getContactInfo())
                .cardInfo(order.getCardInfo())
                .addressInfo(order.getAddressInfo())
                .productResponseDtos(order.getOrderProducts().stream()
                        .map(this::toProductResponseDto).toList())
                .build();
        return orderResponseDto;
    }

    private ProductResponseDto toProductResponseDto(OrderProduct orderProduct) {
        log.info("Product ID {}", orderProduct.getStoreProductId());
        String publicUrl = "https://" + s3bucketName + "." + s3urlPostfix + "/";
        String productImageUrl = Optional.ofNullable(orderProduct.getImageSource())
                .map(imageSource -> publicUrl + imageSource)
                .orElse(null);
        ProductResponseDto productResponseDto = ProductResponseDto
                .builder()
                .id(orderProduct.getId())
                .storeProductId(orderProduct.getStoreProductId())
                .quantity(orderProduct.getQuantity())
                .name(orderProduct.getName())
                .totalPrice(orderProduct.getTotalPrice())
                .extraPrice(orderProduct.getExtraPrice())
                .basePrice(orderProduct.getBasePrice())
                .calculatedCalories(orderProduct.getTotalCalories())
                .imageSource(productImageUrl)
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
                .calculatedPrice(orderCustomRule.getTotalPrice())
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
                .calculatedPrice(orderProductOption.getTotalPrice())
                .basePrice(orderProductOption.getBasePrice())
                .calculatedCalories(orderProductOption.getTotalCalories())
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
                .calculatedPrice(orderProductOptionTrait.getTotalPrice())
                .basePrice(orderProductOptionTrait.getBasePrice())
                .calculatedCalories(orderProductOptionTrait.getCalculatedCalories())
                .optionTraitType(orderProductOptionTrait.getOptionTraitType())
                .selectedValue(orderProductOptionTrait.getSelectedValue())
                .build();
        return traitResponseDto;
    }
}
