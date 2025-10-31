package com.whattheburger.backend.util;

import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.domain.enums.OrderStatus;
import com.whattheburger.backend.domain.enums.OrderType;
import com.whattheburger.backend.domain.enums.PaymentStatus;
import com.whattheburger.backend.domain.order.*;
import com.whattheburger.backend.domain.order.QuantityDetail;
import com.whattheburger.backend.service.dto.cart.*;
import com.whattheburger.backend.service.dto.cart.calculator.ProcessedOptionDto;
import jakarta.persistence.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class OrderSessionFactory {
    public OrderSession createFromCartDto(ProcessedCartDto cartDto, OrderType orderType, Long storeId) {
        OrderSession orderSession = OrderSession
                .builder()
                .sessionId(UUID.randomUUID())
                .storeId(storeId)
                .orderStatus(OrderStatus.PENDING)
                .orderType(orderType)
                .paymentStatus(PaymentStatus.UNPAID)
                .totalPrice(cartDto.getTotalPrice())
                .orderSessionProducts(
                        cartDto.getProcessedProductDtos().stream()
                                .map(this::createFromProductDto)
                                .toList()
                )
                .build();
        log.info("Cart Total {}", cartDto.getTotalPrice());
        return orderSession;
    }

    public void overwriteFromCartDto(ProcessedCartDto cartDto, OrderType orderType, Long storeId, OrderSession orderSession) {
        List<ProcessedProductDto> processedProductDtos = cartDto.getProcessedProductDtos();
        List<OrderSessionProduct> orderSessionProducts = processedProductDtos.stream()
                .map(this::createFromProductDto)
                .toList();
        orderSession.renewOrderSession(
                orderSessionProducts,
                orderType,
                storeId,
                cartDto.getTotalPrice()
        );
    }

    private OrderSessionProduct createFromProductDto(ProcessedProductDto productDto) {
        StoreProduct storeProduct = productDto.getStoreProduct();
        Product product = storeProduct.getProduct();

        BigDecimal productPrice = Optional.ofNullable(storeProduct.getOverridePrice())
                .orElse(product.getPrice());

        log.info("Calculated Product Price {}", productDto.getCalculatedProductPrice());

        return OrderSessionProduct
                .builder()
                .basePrice(productPrice)
                .storeProductId(storeProduct.getId())
                .extraPrice(productDto.getCalculatedExtraPrice())
                .productType(product.getProductType())
                .name(product.getName())
                .imageSource(product.getImageSource())
                .quantity(productDto.getQuantity())
                .totalCalories(product.getCalories())
                .orderSessionCustomRules(
                        productDto.getProcessedCustomRuleDtos().stream()
                                .map(this::createFromCustomRuleDto)
                                .toList()
                )
                .totalPrice(productDto.getCalculatedProductPrice())
                .build();
    }

    private OrderSessionCustomRule createFromCustomRuleDto(ProcessedCustomRuleDto customRuleDto) {
        CustomRule customRule = customRuleDto.getCustomRule();
        return OrderSessionCustomRule
                .builder()
                .customRuleId(customRule.getId())
                .name(customRule.getName())
                .totalPrice(customRuleDto.getCalculatedCustomRulePrice())
                .orderSessionOptions(
                        customRuleDto.getProcessedOptionDtos().stream()
                                .filter(optionDto -> optionDto.getIsSelected())
                                .map(this::createFromOptionDto)
                                .toList()
                )
                .build();

    }
    private OrderSessionOption createFromOptionDto(ProcessedOptionDto optionDto) {
        ProductOption productOption = optionDto.getProductOption();
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

        return OrderSessionOption
                .builder()
                .productOptionId(productOption.getId())
                .countType(productOption.getCountType())
                .name(productOption.getOption().getName())
                .basePrice(productOption.getExtraPrice())
                .quantityDetail(quantityDetail)
                .quantity(optionDto.getQuantity())
                .totalCalories(productOption.getOption().getCalories())
                .totalPrice(optionDto.getCalculatedOptionPrice())
                .orderSessionOptionTraits(
                        optionDto.getProcessedTraitDtos().stream()
                                .map(this::createFromTraitDto)
                                .toList()
                )
                .build();
    }

    private OrderSessionOptionTrait createFromTraitDto(ProcessedTraitDto traitDto) {
        ProductOptionTrait productOptionTrait = traitDto.getProductOptionTrait();
        return OrderSessionOptionTrait
                .builder()
                .basePrice(productOptionTrait.getExtraPrice())
                .calculatedCalories(productOptionTrait.getExtraCalories())
                .labelCode(productOptionTrait.getOptionTrait().getLabelCode())
                .optionTraitType(productOptionTrait.getOptionTrait().getOptionTraitType())
                .name(productOptionTrait.getOptionTrait().getName())
                .productOptionTraitId(productOptionTrait.getId())
                .selectedValue(traitDto.getCurrentValue())
                .totalPrice(traitDto.getCalculatedTraitPrice())
                .build();
    }
}
