package com.whattheburger.backend.controller.dto_mapper;

import com.whattheburger.backend.controller.dto.cart.*;
import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.service.dto.cart.ProcessedCartDto;
import com.whattheburger.backend.service.dto.cart.ProcessedCustomRuleDto;
import com.whattheburger.backend.service.dto.cart.ProcessedProductDto;
import com.whattheburger.backend.service.dto.cart.ProcessedTraitDto;
import com.whattheburger.backend.service.dto.cart.calculator.ProcessedOptionDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class CartResponseDtoMapper {
    @Value("${aws.s3-bucket-name}")
    private String s3bucketName;
    @Value("${aws.s3-url-postfix}")
    private String s3postFix;
    public ProductResponseDto toProductResponse(
            ProcessedProductDto processedProductDto
    ) {
        String publicUrl = "https://" + s3bucketName + "." + s3postFix + "/";
        StoreProduct storeProduct = processedProductDto.getStoreProduct();
        Product product = storeProduct.getProduct();
        String productImageUrl = Optional.ofNullable(product.getImageSource())
                .map(imageSource -> publicUrl + imageSource)
                .orElse(null);

        // Override price
        BigDecimal productPrice = Optional.ofNullable(storeProduct.getOverridePrice())
                .orElse(product.getPrice());

        List<ProcessedCustomRuleDto> processedCustomRuleDtos = processedProductDto.getProcessedCustomRuleDtos();

        List<CustomRuleResponseDto> customRuleResponses = new ArrayList<>();
        for (ProcessedCustomRuleDto processedCustomRuleDto : processedCustomRuleDtos) {
            CustomRule customRule = processedCustomRuleDto.getCustomRule();
            BigDecimal customRuleTotalPrice = processedCustomRuleDto.getCalculatedCustomRulePrice();
            List<ProcessedOptionDto> processedOptionDtos = processedCustomRuleDto.getProcessedOptionDtos();
            List<OptionResponseDto> optionResponses = new ArrayList<>();
            for (ProcessedOptionDto processedOptionDto : processedOptionDtos) {
                ProductOption productOption = processedOptionDto.getProductOption();
                String optionImageUrl = Optional.ofNullable(productOption.getOption().getImageSource())
                        .map(imageSource -> publicUrl + imageSource)
                        .orElse(null);

                QuantityDetailResponse quantityDetailResponse = Optional.ofNullable(processedOptionDto.getProcessedQuantityDto())
                        .map(processedQuantityDto ->
                                new QuantityDetailResponse(
                                        processedQuantityDto.getProductOptionOptionQuantities().stream()
                                                .map(optionQuantity -> new QuantityDetail(
                                                        optionQuantity.getId(),
                                                        optionQuantity.getOptionQuantity().getQuantity().getLabelCode(),
                                                        optionQuantity.getExtraPrice(),
                                                        optionQuantity.getOptionQuantity().getExtraCalories(),
                                                        optionQuantity.getIsDefault(),
                                                        optionQuantity.getOptionQuantity().getQuantity().getQuantityType()
                                                )).toList(),
                                        processedQuantityDto.getSelectedQuantity().getId()
                                ))
                        .orElse(null);

                List<ProcessedTraitDto> processedTraitDtos = processedOptionDto.getProcessedTraitDtos();
                List<OptionTraitResponseDto> optionTraitResponses = new ArrayList<>();

                for (ProcessedTraitDto processedTraitDto : processedTraitDtos) {
                    ProductOptionTrait productOptionTrait = processedTraitDto.getProductOptionTrait();
                    optionTraitResponses.add(
                            OptionTraitResponseDto
                                    .builder()
                                    .productOptionTraitId(productOptionTrait.getId())
                                    .baseCalories(productOptionTrait.getExtraCalories())
                                    .basePrice(productOptionTrait.getExtraPrice())
                                    .currentValue(processedTraitDto.getCurrentValue())
                                    .labelCode(productOptionTrait.getOptionTrait().getLabelCode())
                                    .defaultSelection(productOptionTrait.getDefaultSelection())
                                    .optionTraitName(productOptionTrait.getOptionTrait().getName())
                                    .optionTraitType(productOptionTrait.getOptionTrait().getOptionTraitType())
                                    .traitTotalPrice(processedTraitDto.getCalculatedTraitPrice())
                                    .build()
                    );
                }
                optionResponses.add(
                        OptionResponseDto
                                .builder()
                                .productOptionId(productOption.getId())
                                .baseCalories(productOption.getOption().getCalories())
                                .basePrice(productOption.getExtraPrice())
                                .countType(productOption.getCountType())
                                .defaultQuantity(productOption.getDefaultQuantity())
                                .imageSource(optionImageUrl)
                                .isDefault(productOption.getIsDefault())
                                .isSelected(processedOptionDto.getIsSelected())
                                .maxQuantity(productOption.getMaxQuantity())
                                .measureType(productOption.getMeasureType())
                                .optionName(productOption.getOption().getName())
                                .optionQuantity(processedOptionDto.getQuantity())
                                .optionTotalPrice(processedOptionDto.getCalculatedOptionPrice())
                                .optionTraitResponses(optionTraitResponses)
                                .orderIndex(productOption.getOrderIndex())
                                .quantityDetailResponse(quantityDetailResponse)
                                .build()
                );
            }
            customRuleResponses.add(
                    CustomRuleResponseDto
                            .builder()
                            .customRuleId(customRule.getId())
                            .optionResponses(optionResponses)
                            .customRuleName(customRule.getName())
                            .customRuleTotalPrice(customRuleTotalPrice)
                            .customRuleType(customRule.getCustomRuleType())
                            .maxSelection(customRule.getMaxSelection())
                            .minSelection(customRule.getMinSelection())
                            .orderIndex(customRule.getOrderIndex())
                            .build()
            );
        }
        ProductResponseDto productResponse = ProductResponseDto
                .builder()
                .customRuleResponses(customRuleResponses)
                .basePrice(productPrice)
                .storeProductId(storeProduct.getId())
                .productName(product.getName())
                .briefInfo(product.getBriefInfo())
                .calories(product.getCalories())
                .productExtraPrice(processedProductDto.getCalculatedExtraPrice())
                .productTotalPrice(processedProductDto.getCalculatedProductPrice())
                .productType(product.getProductType())
                .imageSource(productImageUrl)
                .quantity(processedProductDto.getQuantity())
                .build();

        return productResponse;
    }
    public CartResponseDto toCartResponseDto(
            ProcessedCartDto processedCartDto
    ) {
        List<ProcessedProductDto> processedProductDtos = processedCartDto.getProcessedProductDtos();
        BigDecimal cartTotalPrice = processedCartDto.getTotalPrice();

        List<ProductResponseDto> productResponses = processedProductDtos.stream()
                .map(this::toProductResponse)
                .toList();
        return new CartResponseDto(
                productResponses,
                cartTotalPrice
        );
    }
}
