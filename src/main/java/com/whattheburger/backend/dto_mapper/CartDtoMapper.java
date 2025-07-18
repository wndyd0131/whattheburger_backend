package com.whattheburger.backend.dto_mapper;

import com.whattheburger.backend.controller.dto.cart.*;
import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.domain.cart.Cart;
import com.whattheburger.backend.service.dto.cart.*;
import com.whattheburger.backend.service.dto.cart.ProductDetail;
import com.whattheburger.backend.service.dto.cart.calculator.CalculatorDto;
import com.whattheburger.backend.service.dto.cart.calculator.TraitDetail;
import com.whattheburger.backend.service.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class CartDtoMapper {
//    public List<ValidatedCartDto> toValidatedCartDtos(
//            List<Cart> carts,
//            Map<Long, Product> productMap,
//            Map<Long, CustomRule> customRuleMap,
//            Map<Long, ProductOption> productOptionMap,
//            Map<Long, ProductOptionTrait> productOptionTraitMap,
//            Map<Long, ProductOptionOptionQuantity> quantityMap
//    ) {
//
//        List<ValidatedCartDto> validatedCartDtos = new ArrayList<>();
//
//        for (Cart cart : carts) {
//            Long productId = cart.getProductId();
//            log.info("Cart {}", productId);
//            Product product = Optional.ofNullable(productMap.get(productId))
//                    .orElseThrow(() -> new ProductNotFoundException(productId));
//
//            ProductDetail productDetail = new ProductDetail(
//                    productId,
//                    product.getName(),
//                    product.getProductType(),
//                    product.getPrice(),
//                    product.getImageSource()
//            );
//
//            List<CustomRuleRequest> customRuleRequests = cart.getCustomRuleRequests();
//            List<CustomRuleDetail> customRuleDetails = new ArrayList<>();
//
//            for (CustomRuleRequest customRuleRequest : customRuleRequests) {
//                Long customRuleId = customRuleRequest.getCustomRuleId();
//                CustomRule customRule = Optional.ofNullable(customRuleMap.get(customRuleId))
//                        .orElseThrow(() -> new CustomRuleNotFoundException(customRuleId));
//                List<OptionRequest> optionRequests = customRuleRequest.getOptionRequests();
//                List<OptionDetail> optionDetails = new ArrayList<>();
//                log.info("customRuleName: {}", customRule.getName());
//                for (OptionRequest optionRequest : optionRequests) {
//                    log.info("POOQ_ID {}", optionRequest.getQuantityDetailRequest());
//                    Long productOptionId = optionRequest.getProductOptionId();
//
//                    ProductOption productOption = Optional.ofNullable(productOptionMap.get(productOptionId))
//                            .orElseThrow(() -> new ProductOptionNotFoundException(productOptionId));
//
//                    log.info("quantityDetailRequest {}", optionRequest.getQuantityDetailRequest());
//                    QuantityDetail quantityDetail = Optional.ofNullable(optionRequest.getQuantityDetailRequest())
//                            .map(quantityDetailRequest -> {
//                                log.info("POOQID {}", quantityDetailRequest);
//
//                                ProductOptionOptionQuantity productOptionOptionQuantity = Optional.ofNullable(quantityMap.get(quantityDetailRequest.getId()))
//                                        .orElseThrow(() -> new POOQuantityNotFoundException(quantityDetailRequest.getId()));
//                                return new QuantityDetail(
//                                        productOptionOptionQuantity.getId(),
//                                        productOptionOptionQuantity.getOptionQuantity().getQuantity().getQuantityType()
//                                );
//                            })
//                            .orElse(null);
//
//                    List<OptionTraitRequest> optionTraitRequests = optionRequest.getOptionTraitRequests();
//                    List<OptionTraitDetail> optionTraitDetails = new ArrayList<>();
//                    log.info("productOptionName: {}", productOption.getOption().getName());
//                    for (OptionTraitRequest optionTraitRequest : optionTraitRequests) {
//                        Long productOptionTraitId = optionTraitRequest.getProductOptionTraitId();
//                        ProductOptionTrait productOptionTrait = Optional.ofNullable(productOptionTraitMap.get(productOptionTraitId))
//                                .orElseThrow(() -> new ProductOptionTraitNotFoundException(productOptionTraitId));
//                        optionTraitDetails.add(
//                                new OptionTraitDetail(
//                                        productOptionTraitId,
//                                        optionTraitRequest.getCurrentValue(),
//                                        productOptionTrait.getOptionTrait().getLabelCode(),
//                                        productOptionTrait.getOptionTrait().getName(),
//                                        productOptionTrait.getOptionTrait().getOptionTraitType()
//                                )
//                        );
//                    }
//                    optionDetails.add(
//                            new OptionDetail(
//                                    productOptionId,
//                                    optionRequest.getOptionQuantity(),
//                                    optionRequest.getIsSelected(),
//                                    optionTraitDetails,
//                                    productOption.getCountType(),
//                                    productOption.getMeasureType(),
//                                    productOption.getOption().getName(),
//                                    productOption.getOrderIndex(),
//                                    quantityDetail
//                            )
//                    );
//                }
//                customRuleDetails.add(
//                        new CustomRuleDetail(
//                                customRuleId,
//                                customRule.getName(),
//                                customRule.getOrderIndex(),
//                                optionDetails
//                        )
//                );
//            }
//
//            validatedCartDtos.add(
//                    new ValidatedCartDto(
//                            productDetail,
//                            customRuleDetails,
//                            cart.getQuantity()
//                    )
//            );
//        }
//        return validatedCartDtos;
//    }

    public CalculatorDto toCalculatorDto(
            List<Cart> carts,
            Map<Long, Product> productMap,
            Map<Long, CustomRule> customRuleMap,
            Map<Long, ProductOption> productOptionMap,
            Map<Long, ProductOptionTrait> productOptionTraitMap,
            Map<Long, ProductOptionOptionQuantity> quantityMap
    ) {
        List<com.whattheburger.backend.service.dto.cart.calculator.ProductDetail> productDetails = carts.stream()
                .map(cart -> {
                    Product product = productMap.get(cart.getProductId());
                    List<com.whattheburger.backend.service.dto.cart.calculator.OptionDetail> optionDetails = cart.getCustomRuleRequests().stream()
                            .flatMap(customRuleRequest -> customRuleRequest.getOptionRequests().stream())
                            .map(optionRequest -> {
                                ProductOption productOption = productOptionMap.get(optionRequest.getProductOptionId());
                                // quantity handling
                                com.whattheburger.backend.service.dto.cart.calculator.QuantityDetail quantityDetail = Optional.ofNullable(optionRequest.getQuantityDetailRequest())
                                        .map(quantityDetailRequest -> {
                                            ProductOptionOptionQuantity productOptionOptionQuantity = productOption.getProductOptionOptionQuantities().stream()
                                                    .filter(pooQuantity -> pooQuantity.getIsDefault())
                                                    .findFirst()
                                                    .orElseThrow(() -> new IllegalStateException("Missing default uncountable quantity value for productOption " + productOption.getId()));
                                            return new com.whattheburger.backend.service.dto.cart.calculator.QuantityDetail(
                                                    quantityMap.get(quantityDetailRequest.getId()).getExtraPrice(),
                                                    quantityDetailRequest.getId(),
                                                    productOptionOptionQuantity.getId()
                                            );
                                                }
                                        ).orElse(null);
                                List<TraitDetail> traitDetails = optionRequest.getOptionTraitRequests().stream()
                                        .map(optionTraitRequest -> {
                                            ProductOptionTrait optionTrait = productOptionTraitMap.get(optionTraitRequest.getProductOptionTraitId());
                                            return new TraitDetail(
                                                    optionTrait.getExtraPrice(),
                                                    optionTrait.getDefaultSelection(),
                                                    optionTraitRequest.getCurrentValue(),
                                                    optionTrait.getOptionTrait().getOptionTraitType()
                                            );
                                        })
                                        .toList();

                                return com.whattheburger.backend.service.dto.cart.calculator.OptionDetail
                                        .builder()
                                        .price(productOption.getExtraPrice())
                                        .isDefault(productOption.getIsDefault())
                                        .defaultQuantity(productOption.getDefaultQuantity())
                                        .isSelected(optionRequest.getIsSelected())
                                        .quantity(optionRequest.getOptionQuantity())
                                        .quantityDetail(quantityDetail)
                                        .traitDetails(traitDetails)
                                        .build();
                            })
                            .toList();
                    return new com.whattheburger.backend.service.dto.cart.calculator.ProductDetail(
                            product.getPrice(),
                            cart.getQuantity(),
                            optionDetails
                    );
                })
                .toList();

        return CalculatorDto
                .builder()
                .productDetails(productDetails)
                .build();
    }

    public CartResponse toCartResponse(
            ValidatedCartDto validatedCartDto
    ) {
        ValidatedProduct validatedProduct = validatedCartDto.getValidatedProduct();
        List<ValidatedCustomRule> validatedCustomRules = validatedCartDto.getValidatedCustomRules();

        Product product = validatedProduct.getProduct();

        ProductResponse productResponse = new ProductResponse(
                product.getId(),
                product.getName(),
                product.getProductType(),
                product.getPrice(),
                product.getImageSource()
        );
        List<CustomRuleResponse> customRuleResponses = new ArrayList<>();

        for (ValidatedCustomRule validatedCustomRule : validatedCustomRules) {
            CustomRule customRule = validatedCustomRule.getCustomRule();
            List<ValidatedOption> validatedOptions = validatedCustomRule.getValidatedOptions();
            List<OptionResponse> optionResponses = new ArrayList<>();
            for (ValidatedOption validatedOption : validatedOptions) {
                ProductOption productOption = validatedOption.getProductOption();

                QuantityDetailResponse quantityDetailResponse = Optional.ofNullable(validatedOption.getValidatedQuantity())
                        .map(validatedQuantity ->
                                new QuantityDetailResponse(
                                        validatedQuantity.getProductOptionOptionQuantity().getId(),
                                        validatedQuantity.getProductOptionOptionQuantity().getOptionQuantity().getQuantity().getQuantityType()
                                ))
                        .orElse(null);

                List<ValidatedTrait> validatedTraits = validatedOption.getValidatedTraits();
                List<OptionTraitResponse> optionTraitResponses = new ArrayList<>();

                for (ValidatedTrait validatedTrait : validatedTraits) {
                    ProductOptionTrait productOptionTrait = validatedTrait.getProductOptionTrait();
                    optionTraitResponses.add(
                            new OptionTraitResponse(
                                    productOptionTrait.getId(),
                                    validatedTrait.getCurrentValue(),
                                    productOptionTrait.getOptionTrait().getLabelCode(),
                                    productOptionTrait.getOptionTrait().getName(),
                                    productOptionTrait.getOptionTrait().getOptionTraitType()
                            )
                    );
                }
                optionResponses.add(
                        new OptionResponse(
                                productOption.getId(),
                                validatedOption.getQuantity(),
                                validatedOption.getIsSelected(),
                                optionTraitResponses,
                                productOption.getCountType(),
                                productOption.getMeasureType(),
                                productOption.getOption().getName(),
                                productOption.getOrderIndex(),
                                quantityDetailResponse
                        )
                );
            }
            customRuleResponses.add(
                    new CustomRuleResponse(
                            customRule.getId(),
                            customRule.getName(),
                            customRule.getOrderIndex(),
                            optionResponses
                    )
            );
        }

        return new CartResponse(
                productResponse,
                customRuleResponses,
                validatedCartDto.getQuantity()
        );
    }
    public CartResponseDto toCartResponseDto(
            ProcessedCartDto processedCartDto
    ) {
        List<ValidatedCartDto> validatedCartDtos = processedCartDto.getCartDtos();
        Double totalPrice = processedCartDto.getTotalPrice();

        List<CartResponse> cartResponses = validatedCartDtos.stream()
                .map(this::toCartResponse)
                .toList();
        return new CartResponseDto(
                cartResponses,
                totalPrice
        );
    }
}
