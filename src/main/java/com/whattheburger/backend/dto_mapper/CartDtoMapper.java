package com.whattheburger.backend.dto_mapper;

import com.whattheburger.backend.controller.dto.cart.*;
import com.whattheburger.backend.controller.dto.cart.QuantityDetail;
import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.service.dto.cart.*;
import com.whattheburger.backend.service.dto.cart.calculator.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
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

    public ProcessedProductDto toProcessedProductDto(
            ValidatedCartDto validatedCartDto,
            ProductCalcDetail productCalcDetail
    ) {
        ValidatedProduct validatedProduct = validatedCartDto.getValidatedProduct();
        if (!(validatedProduct.getProduct().getId().equals(productCalcDetail.getProductId())))
            throw new IllegalStateException("validated cart and calculated cart are different in id");
        List<ValidatedCustomRule> validatedCustomRules = validatedCartDto.getValidatedCustomRules();
        List<CustomRuleCalcDetail> customRuleCalcDetails = productCalcDetail.getCustomRuleCalcDetails();
        if (validatedCustomRules.size() != customRuleCalcDetails.size())
            throw new IllegalStateException("validated cart and calculated cart are different in length");
        List<ProcessedCustomRuleDto> processedCustomRuleDtos = new ArrayList<>();
        for (int j = 0; j < validatedCustomRules.size(); j++) {
            ValidatedCustomRule validatedCustomRule = validatedCustomRules.get(j);
            CustomRuleCalcDetail customRuleCalcDetail = customRuleCalcDetails.get(j);
            if (!(validatedCustomRule.getCustomRule().getId().equals(customRuleCalcDetail.getCustomRuleId())))
                throw new IllegalStateException("validated cart and calculated cart are different in id");

            List<ValidatedOption> validatedOptions = validatedCustomRule.getValidatedOptions();
            List<OptionCalcDetail> optionCalcDetails = customRuleCalcDetail.getOptionCalcDetails();
            if (validatedOptions.size() != optionCalcDetails.size())
                throw new IllegalStateException("validated cart and calculated cart are different in length");
            List<ProcessedOptionDto> processedOptionDtos = new ArrayList<>();
            for (int k = 0; k < validatedOptions.size(); k++) {
                ValidatedOption validatedOption = validatedOptions.get(k);
                OptionCalcDetail optionCalcDetail = optionCalcDetails.get(k);
                if (!(validatedOption.getProductOption().getId().equals(optionCalcDetail.getProductOptionId())))
                    throw new IllegalStateException("validated cart and calculated cart are different in id");

                ProcessedQuantityDto processedQuantityDto = Optional.ofNullable(validatedOption.getValidatedQuantity())
                        .map(validatedQuantity ->
                                Optional.ofNullable(optionCalcDetail.getQuantityCalcDetail())
                                        .map(quantityCalcDetail -> {
                                            if (!(validatedQuantity.getSelectedQuantity().getId().equals(quantityCalcDetail.getRequestedId())))
                                                throw new IllegalStateException("validated quantity and calculated quantity have different ids [" + validatedQuantity.getSelectedQuantity().getId() + "]" + "vs." + "[" + quantityCalcDetail.getRequestedId() + "]");
                                            return new ProcessedQuantityDto(
                                                    validatedQuantity.getProductOptionOptionQuantities(),
                                                    validatedQuantity.getSelectedQuantity()
                                            );
                                        })
                                        .orElseGet(() -> {
                                            if (optionCalcDetail.getQuantityCalcDetail() == null)
                                                return null;
                                            throw new IllegalStateException("validated cart and calculated cart are different in id");
                                        })
                        )
                        .orElseGet(() -> {
                            if (optionCalcDetail.getQuantityCalcDetail() == null)
                                return null;
                            throw new IllegalStateException("validated cart and calculated cart are different in id");
                        });

                List<ValidatedTrait> validatedTraits = validatedOption.getValidatedTraits();
                List<TraitCalcDetail> traitCalcDetails = optionCalcDetail.getTraitCalcDetails();
                if (validatedTraits.size() != traitCalcDetails.size())
                    throw new IllegalStateException("validated cart and calculated cart are different in length");
                List<ProcessedTraitDto> processedTraitDtos = new ArrayList<>();
                for (int l = 0; l < validatedTraits.size(); l++) {
                    ValidatedTrait validatedTrait = validatedTraits.get(l);
                    TraitCalcDetail traitCalcDetail = traitCalcDetails.get(l);
                    if (!(validatedTrait.getProductOptionTrait().getId().equals(traitCalcDetail.getProductOptionTraitId())))
                        throw new IllegalStateException("validated cart and calculated cart are different in id");

                    ProcessedTraitDto processedTraitDto = new ProcessedTraitDto(
                            validatedTrait.getProductOptionTrait(),
                            validatedTrait.getCurrentValue(),
                            traitCalcDetail.getPrice()
                    );
                    processedTraitDtos.add(processedTraitDto);
                }
                ProcessedOptionDto processedOptionDto = new ProcessedOptionDto(
                        validatedOption.getProductOption(),
                        validatedOption.getQuantity(),
                        validatedOption.getIsSelected(),
                        processedQuantityDto,
                        processedTraitDtos,
                        optionCalcDetail.getTraitTotalPrice()
                );
                processedOptionDtos.add(processedOptionDto);
            }
            ProcessedCustomRuleDto processedCustomRuleDto = new ProcessedCustomRuleDto(
                    validatedCustomRule.getCustomRule(),
                    processedOptionDtos,
                    customRuleCalcDetail.getOptionTotalPrice()
            );
            processedCustomRuleDtos.add(processedCustomRuleDto);
        }
        ProcessedProductDto processedProductDto = new ProcessedProductDto(
                validatedProduct.getProduct(),
                validatedProduct.getQuantity(),
                processedCustomRuleDtos,
                productCalcDetail.getCalculatedProductPrice(),
                productCalcDetail.getCustomRuleTotalPrice()
        );
        return processedProductDto;
    }

    public ProcessedCartDto toProcessedCartDto(
            List<ValidatedCartDto> validatedCartDtos,
            CalculatedCartDto calculatedCartDto
    ) {
        List<ProductCalcDetail> productCalcDetails = calculatedCartDto.getProductCalcDetails();
        BigDecimal cartTotalPrice = calculatedCartDto.getTotalPrice();

        if (validatedCartDtos.size() != productCalcDetails.size())
            throw new IllegalStateException("validated cart and calculated cart are different in length");
        List<ProcessedProductDto> processedProductDtos = new ArrayList<>();
        for (int i = 0; i < validatedCartDtos.size(); i++) {
            ValidatedCartDto validatedCartDto = validatedCartDtos.get(i);
            ProductCalcDetail productCalcDetail = productCalcDetails.get(i);

            ProcessedProductDto processedProductDto = toProcessedProductDto(validatedCartDto, productCalcDetail);

            processedProductDtos.add(processedProductDto);
        }

        return new ProcessedCartDto(
                processedProductDtos,
                cartTotalPrice
        );
    }
//
//    public CalculatorDto toCalculatorDto(
//            List<Cart> carts,
//            Map<Long, Product> productMap,
//            Map<Long, CustomRule> customRuleMap,
//            Map<Long, ProductOption> productOptionMap,
//            Map<Long, ProductOptionTrait> productOptionTraitMap,
//            Map<Long, ProductOptionOptionQuantity> quantityMap
//    ) {
//        List<ProductCalcDetail> productCalcDetails = carts.stream()
//                .map(cart -> {
//                    Product product = productMap.get(cart.getProductId());
//                    List<OptionCalcDetail> optionCalcDetails = cart.getCustomRuleRequests().stream()
//                            .flatMap(customRuleRequest -> customRuleRequest.getOptionRequests().stream())
//                            .map(optionRequest -> {
//                                ProductOption productOption = productOptionMap.get(optionRequest.getProductOptionId());
//                                // quantity handling
//                                QuantityCalcDetail quantityCalcDetail = Optional.ofNullable(optionRequest.getQuantityDetailRequest())
//                                        .map(quantityDetailRequest -> {
//                                            ProductOptionOptionQuantity productOptionOptionQuantity = productOption.getProductOptionOptionQuantities().stream()
//                                                    .filter(pooQuantity -> pooQuantity.getIsDefault())
//                                                    .findFirst()
//                                                    .orElseThrow(() -> new IllegalStateException("Missing default uncountable quantity value for productOption " + productOption.getId()));
//                                            return new QuantityCalcDetail(
//                                                    quantityMap.get(quantityDetailRequest.getId()).getExtraPrice(),
//                                                    quantityDetailRequest.getId(),
//                                                    productOptionOptionQuantity.getId()
//                                            );
//                                                }
//                                        ).orElse(null);
//                                List<TraitCalcDetail> traitCalcDetails = optionRequest.getOptionTraitRequests().stream()
//                                        .map(optionTraitRequest -> {
//                                            ProductOptionTrait optionTrait = productOptionTraitMap.get(optionTraitRequest.getProductOptionTraitId());
//                                            log.info("trait detail trait id {}", optionTrait.getId());
//
//                                            TraitCalcDetail traitCalcDetail = new TraitCalcDetail(
//                                                    optionTrait.getExtraPrice(),
//                                                    optionTrait.getDefaultSelection(),
//                                                    optionTraitRequest.getCurrentValue(),
//                                                    optionTrait.getOptionTrait().getOptionTraitType()
//                                            );
//                                            traitCalculator.calculateTotalPrice()
//                                            new CalculatedTraitDto();
//                                        })
//                                        .toList();
//
//                                return OptionCalcDetail
//                                        .builder()
//                                        .price(productOption.getExtraPrice())
//                                        .isDefault(productOption.getIsDefault())
//                                        .defaultQuantity(productOption.getDefaultQuantity())
//                                        .isSelected(optionRequest.getIsSelected())
//                                        .quantity(optionRequest.getOptionQuantity())
//                                        .quantityCalcDetail(quantityCalcDetail)
//                                        .traitCalcDetails(traitCalcDetails)
//                                        .build();
//                            })
//                            .toList();
//                    return new ProductCalcDetail(
//                            product.getPrice(),
//                            cart.getQuantity(),
//                            optionCalcDetails
//                    );
//                })
//                .toList();
//
//        return CalculatorDto
//                .builder()
//                .productCalcDetails(productCalcDetails)
//                .build();
//    }
}
