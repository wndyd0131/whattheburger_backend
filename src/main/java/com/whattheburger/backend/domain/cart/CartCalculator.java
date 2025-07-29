package com.whattheburger.backend.domain.cart;

import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.dto_mapper.CartDtoMapper;
import com.whattheburger.backend.service.dto.cart.calculator.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class CartCalculator {
    private final ProductCalculator productCalculator;
    private final CustomRuleCalculator customRuleCalculator;
    private final OptionCalculator optionCalculator;
    private final TraitCalculator traitCalculator;

    public CalculatedCartDto calculate(
            List<Cart> carts,
            Map<Long, Product> productMap,
            Map<Long, CustomRule> customRuleMap,
            Map<Long, ProductOption> productOptionMap,
            Map<Long, ProductOptionTrait> productOptionTraitMap,
            Map<Long, ProductOptionOptionQuantity> quantityMap
    ) {
        List<ProductCalcDetail> productCalcDetails = carts.stream()
                .map(cart -> calculate(cart, productMap, customRuleMap, productOptionMap, productOptionTraitMap, quantityMap))
                .toList();

        BigDecimal cartPrice = productCalculator.calculateTotalPrice(productCalcDetails);

        return CalculatedCartDto
                .builder()
                .productCalcDetails(productCalcDetails)
                .totalPrice(cartPrice)
                .build();
    }

    public ProductCalcDetail calculate(
            Cart cart,
            Map<Long, Product> productMap,
            Map<Long, CustomRule> customRuleMap,
            Map<Long, ProductOption> productOptionMap,
            Map<Long, ProductOptionTrait> productOptionTraitMap,
            Map<Long, ProductOptionOptionQuantity> quantityMap
    ) {
        Product product = productMap.get(cart.getProductId());
        List<CustomRuleCalcDetail> customRuleCalcDetails = cart.getCustomRuleRequests().stream()
                .map(customRuleRequest -> {
                    CustomRule customRule = customRuleMap.get(customRuleRequest.getCustomRuleId());
                    List<OptionCalcDetail> optionCalcDetails = customRuleRequest.getOptionRequests().stream()
                            .map(optionRequest -> {
                                ProductOption productOption = productOptionMap.get(optionRequest.getProductOptionId());
                                // quantity handling
                                QuantityCalcDetail quantityCalcDetail = Optional.ofNullable(optionRequest.getQuantityDetailRequest())
                                        .map(quantityDetailRequest -> {
                                                    ProductOptionOptionQuantity productOptionOptionQuantity = productOption.getProductOptionOptionQuantities().stream()
                                                            .filter(pooQuantity -> pooQuantity.getIsDefault())
                                                            .findFirst()
                                                            .orElseThrow(() -> new IllegalStateException("Missing default uncountable quantity value for productOption " + productOption.getId()));
                                                    return new QuantityCalcDetail(
                                                            quantityMap.get(quantityDetailRequest.getId()).getExtraPrice(),
                                                            quantityDetailRequest.getId(),
                                                            productOptionOptionQuantity.getId()
                                                    );
                                                }
                                        ).orElse(null);
                                List<TraitCalcDetail> traitCalcDetails = optionRequest.getOptionTraitRequests().stream()
                                        .map(optionTraitRequest -> {
                                            ProductOptionTrait optionTrait = productOptionTraitMap.get(optionTraitRequest.getProductOptionTraitId());
                                            log.info("trait detail trait id {}", optionTrait.getId());

                                            return new TraitCalcDetail(
                                                    optionTrait.getId(),
                                                    optionTrait.getExtraPrice(),
                                                    optionTrait.getDefaultSelection(),
                                                    optionTraitRequest.getCurrentValue(),
                                                    optionTrait.getOptionTrait().getOptionTraitType()
                                            );
                                        }).toList();

                                BigDecimal traitTotalPrice = traitCalculator.calculateTotalPrice(traitCalcDetails);

                                return OptionCalcDetail
                                        .builder()
                                        .productOptionId(productOption.getId())
                                        .price(productOption.getExtraPrice())
                                        .isDefault(productOption.getIsDefault())
                                        .defaultQuantity(productOption.getDefaultQuantity())
                                        .isSelected(optionRequest.getIsSelected())
                                        .quantity(optionRequest.getOptionQuantity())
                                        .quantityCalcDetail(quantityCalcDetail)
                                        .traitCalcDetails(traitCalcDetails)
                                        .traitTotalPrice(traitTotalPrice)
                                        .build();
                            }).toList();
                    BigDecimal optionTotalPrice = optionCalculator.calculateTotalPrice(optionCalcDetails);

                    return CustomRuleCalcDetail
                            .builder()
                            .customRuleId(customRule.getId())
                            .optionCalcDetails(optionCalcDetails)
                            .optionTotalPrice(optionTotalPrice)
                            .build();
                }).toList();
        BigDecimal customRuleTotalPrice = customRuleCalculator.calculateTotalPrice(customRuleCalcDetails);
        ProductCalcDetail productCalcDetail = ProductCalcDetail
                .builder()
                .productId(product.getId())
                .basePrice(product.getPrice())
                .quantity(cart.getQuantity())
                .customRuleCalcDetails(customRuleCalcDetails)
                .customRuleTotalPrice(customRuleTotalPrice)
                .build();
        BigDecimal calculatedProductPrice = productCalculator.calculatePrice(productCalcDetail);
        productCalcDetail.changeCalculatedProductPrice(calculatedProductPrice);
        return productCalcDetail;
    }
}
