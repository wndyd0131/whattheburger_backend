package com.whattheburger.backend.domain.cart;

import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.domain.enums.DeltaType;
import com.whattheburger.backend.service.dto.cart.calculator.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class CartCalculator {
    private final ProductCalculator productCalculator;
    private final CustomRuleCalculator customRuleCalculator;
    private final OptionCalculator optionCalculator;
    private final TraitCalculator traitCalculator;

    public CalculatedCartDto calculateTotalPrice(
            List<Cart> carts,
            Map<Long, StoreProduct> storeProductMap,
            Map<Long, CustomRule> customRuleMap,
            Map<Long, ProductOption> productOptionMap,
            Map<Long, ProductOptionTrait> productOptionTraitMap,
            Map<Long, ProductOptionOptionQuantity> quantityMap
    ) {
        List<ProductCalculationDetail> productCalculationDetails = carts.stream()
                .map(cart -> calculateProductPrice(cart, storeProductMap, customRuleMap, productOptionMap, productOptionTraitMap, quantityMap))
                .toList();
        BigDecimal cartTotalPrice = productCalculationDetails.stream()
                .map(productCalculationDetail -> {
                    BigDecimal quantity = BigDecimal.valueOf(productCalculationDetail.getQuantity());
                    return productCalculationDetail.getCalculatedTotalPrice().multiply(quantity);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        CartCalculationResult cartCalculationResult = new CartCalculationResult(
                productCalculationDetails,
                cartTotalPrice
        );

        return new CalculatedCartDto(cartCalculationResult);
    }

    private CustomRuleCalculationResult calculateExtraPrice(
            Cart cart,
            Map<Long, CustomRule> customRuleMap,
            Map<Long, ProductOption> productOptionMap,
            Map<Long, StoreOptionDelta> optionDeltaMap,
            Map<Long, ProductOptionTrait> productOptionTraitMap,
            Map<Long, ProductOptionOptionQuantity> quantityMap
    ) {
        List<CustomRuleCalculatorDto> customRuleCalculatorDtos = cart.getCustomRuleRequests().stream()
                .map(customRuleRequest -> {
                    CustomRule customRule = customRuleMap.get(customRuleRequest.getCustomRuleId());
                    List<OptionCalculatorDto> optionCalculatorDtos = customRuleRequest.getOptionRequests().stream()
                            .map(optionRequest -> {
                                ProductOption productOption = productOptionMap.get(optionRequest.getProductOptionId());

                                record OptionDelta(BigDecimal price) {}
                                // Option conditioning
                                OptionDelta optionDelta = Optional.ofNullable(optionDeltaMap.get(productOption.getId()))
                                        .map(storeOptionDelta -> {
                                            if (storeOptionDelta.getDeltaType() == DeltaType.OVERRIDE) {
                                                return new OptionDelta(
                                                        storeOptionDelta.getOverridePrice()
                                                );
                                            } else {
                                                return new OptionDelta(
                                                        null
                                                );
                                            }
                                        })
                                        .orElse(
                                                new OptionDelta(productOption.getExtraPrice())
                                        );

                                // quantity handling
                                QuantityCalculatorDto quantityCalculatorDto = Optional.ofNullable(optionRequest.getQuantityDetailRequest())
                                        .map(quantityDetailRequest -> {
                                                    ProductOptionOptionQuantity productOptionOptionQuantity = productOption.getProductOptionOptionQuantities().stream()
                                                            .filter(pooQuantity -> pooQuantity.getIsDefault())
                                                            .findFirst()
                                                            .orElseThrow(() -> new IllegalStateException("Missing default uncountable quantity value for productOption " + productOption.getId()));
                                                    return new QuantityCalculatorDto(
                                                            quantityMap.get(quantityDetailRequest.getId()).getExtraPrice(),
                                                            quantityDetailRequest.getId(),
                                                            productOptionOptionQuantity.getId()
                                                    );
                                                }
                                        ).orElse(null);
                                List<TraitCalculatorDto> traitCalculatorDtos = optionRequest.getOptionTraitRequests().stream()
                                        .map(optionTraitRequest -> {
                                            ProductOptionTrait optionTrait = productOptionTraitMap.get(optionTraitRequest.getProductOptionTraitId());
                                            log.info("trait detail trait id {}", optionTrait.getId());

                                            return new TraitCalculatorDto(
                                                    optionTrait.getId(),
                                                    optionTrait.getExtraPrice(),
                                                    optionTrait.getDefaultSelection(),
                                                    optionTraitRequest.getCurrentValue(),
                                                    optionTrait.getOptionTrait().getOptionTraitType()
                                            );
                                        }).toList();

                                TraitCalculationResult traitCalculationResult = traitCalculator.calculateTotalPrice(traitCalculatorDtos); // calculate traits per option
                                log.info("option {}, trait size {}", productOption.getOption().getName(), traitCalculatorDtos.size());
                                log.info("trait calc result size {}", traitCalculationResult.getTraitCalculationDetails().size());

                                return OptionCalculatorDto
                                        .builder()
                                        .productOptionId(productOption.getId())
                                        .price(optionDelta.price)
                                        .isDefault(productOption.getIsDefault())
                                        .defaultQuantity(productOption.getDefaultQuantity())
                                        .isSelected(optionRequest.getIsSelected())
                                        .quantity(optionRequest.getOptionQuantity())
                                        .quantityCalculatorDto(quantityCalculatorDto)
                                        .traitCalculationResult(traitCalculationResult)
                                        .build();
                            }).toList();
                    OptionCalculationResult optionCalculationResult = optionCalculator.calculateTotalPrice(optionCalculatorDtos); // calculate option per customRule
                    log.info("customRule {}", customRule.getName());
                    log.info("option trait calc result size {}", optionCalculationResult.getOptionCalculationDetails().get(0).getTraitCalculationDetails().size());

                    return CustomRuleCalculatorDto
                            .builder()
                            .customRuleId(customRule.getId())
                            .optionCalculationResult(optionCalculationResult)
                            .build();
                }).toList();
        CustomRuleCalculationResult customRuleCalculationResult = customRuleCalculator.calculateTotalPrice(customRuleCalculatorDtos);
        return customRuleCalculationResult;
    }

    public ProductCalculationDetail calculateProductPrice(
            Cart cart,
            Map<Long, StoreProduct> storeProductMap,
            Map<Long, CustomRule> customRuleMap,
            Map<Long, ProductOption> productOptionMap,
            Map<Long, ProductOptionTrait> productOptionTraitMap,
            Map<Long, ProductOptionOptionQuantity> quantityMap
    ) {
        StoreProduct storeProduct = storeProductMap.get(cart.getStoreProductId());
        Map<Long, StoreOptionDelta> optionDeltaMap = storeProduct.getStoreOptionDeltas().stream()
                .collect(Collectors.toMap(storeOptionDelta -> storeOptionDelta.getProductOption().getId(), Function.identity()));

        BigDecimal productPrice = Optional.ofNullable(storeProduct.getOverridePrice())
                .orElse(storeProduct.getProduct().getPrice());
        CustomRuleCalculationResult customRuleCalculationResult = calculateExtraPrice(
                cart,
                customRuleMap,
                productOptionMap,
                optionDeltaMap,
                productOptionTraitMap,
                quantityMap
        );
        ProductCalculatorDto productCalculatorDto = ProductCalculatorDto
                .builder()
                .storeProductId(storeProduct.getId())
                .basePrice(productPrice)
                .quantity(cart.getQuantity())
                .customRuleCalculationResult(customRuleCalculationResult)
                .build();
        return productCalculator.calculatePrice(productCalculatorDto);
    }
}
