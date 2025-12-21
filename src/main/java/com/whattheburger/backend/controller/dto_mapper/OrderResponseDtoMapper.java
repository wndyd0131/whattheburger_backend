package com.whattheburger.backend.controller.dto_mapper;

import com.whattheburger.backend.controller.dto.order.*;
import com.whattheburger.backend.controller.dto.order.QuantityDetail;
import com.whattheburger.backend.controller.dto.store.NearByStoreReadResponseDto;
import com.whattheburger.backend.controller.dto.store.StoreResponseDto;
import com.whattheburger.backend.domain.Store;
import com.whattheburger.backend.domain.order.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Component
@Slf4j
public class OrderResponseDtoMapper {
    @Value("${aws.s3.public-url}")
    private String s3PublicUrl;

    public List<OrderResponseDto> toOrderResponseDto(List<Order> orders) {
        ArrayList<OrderResponseDto> orderResponseDtos = new ArrayList<>();
        for  (Order order : orders) {
            orderResponseDtos.add(
                    OrderResponseDto
                            .builder()
                            .id(order.getId())
                            .orderNumber(order.getOrderNumber())
                            .orderStatus(order.getOrderStatus())
                            .createdAt(order.getCreatedAt())
                            .updatedAt(order.getUpdatedAt())
                            .orderType(order.getOrderType())
                            .storeId(order.getStore().getId())
                            .totalPrice(order.getTotalPrice())
                            .build()
            );
        }
        return orderResponseDtos;
    }
    public List<OrderDetailResponseDto> toOrderDetailResponseDto(List<Order> orders) {
        ArrayList<OrderDetailResponseDto> orderDetailResponseDtos = new ArrayList<>();
        for  (Order order : orders) {
            orderDetailResponseDtos.add(toOrderDetailResponseDto(order));
        }
        return orderDetailResponseDtos;
    }
    public OrderDetailResponseDto toOrderDetailResponseDto(Order order) {
        OrderDetailResponseDto orderDetailResponseDto = OrderDetailResponseDto
                .builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
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
                .productResponse(order.getOrderProducts().stream()
                        .map(this::toProductResponseDto).toList())
                .storeResponse(toStoreResponseDto(order.getStore()))
                .build();
        return orderDetailResponseDto;
    }

    private StoreResponseDto toStoreResponseDto(Store store) {
        return StoreResponseDto
                .builder()
                .storeId(store.getId())
                .houseNumber(store.getHouseNumber())
                .overpassId(store.getOverpassId())
                .openTime(store.getOpenTime())
                .closeTime(store.getCloseTime())
                .phoneNum(store.getPhoneNum())
                .website(store.getWebsite())
                .branch(store.getBranch())
                .coordinate(
                        new NearByStoreReadResponseDto.CoordinateDto(store.getCoordinate().getLatitude(), store.getCoordinate().getLongitude())
                )
                .address(
                        new NearByStoreReadResponseDto.AddressDto(
                                store.getAddress().getCity(),
                                store.getAddress().getStreet(),
                                store.getAddress().getState(),
                                store.getAddress().getZipcode()
                        )
                )
                .build();
    }

    private ProductResponseDto toProductResponseDto(OrderProduct orderProduct) {
        log.info("Product ID {}", orderProduct.getStoreProductId());
        String productImageUrl = Optional.ofNullable(orderProduct.getImageSource())
                .map(imageSource -> s3PublicUrl + "/" + imageSource)
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
