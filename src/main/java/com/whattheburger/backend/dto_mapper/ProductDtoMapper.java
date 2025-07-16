package com.whattheburger.backend.dto_mapper;

import com.whattheburger.backend.controller.dto.product.ProductCreateRequestDto;
import com.whattheburger.backend.service.dto.product.CustomRuleRequest;
import com.whattheburger.backend.service.dto.product.OptionRequest;
import com.whattheburger.backend.service.dto.product.OptionTraitRequest;
import com.whattheburger.backend.service.dto.product.ProductCreateDto;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductDtoMapper {
    public ProductCreateDto fromControllerDto(ProductCreateRequestDto productCreateRequestDto) {
        return ProductCreateDto
                .builder()
                .productName(productCreateRequestDto.getProductName())
                .productPrice(productCreateRequestDto.getProductPrice())
                .productCalories(productCreateRequestDto.getProductCalories())
                .productType(productCreateRequestDto.getProductType())
                .briefInfo(productCreateRequestDto.getBriefInfo())
                .categoryIds(productCreateRequestDto.getCategoryIds())
                .imageSource(null)
                .customRuleRequests(
                        productCreateRequestDto.getCustomRuleRequests().stream()
                                .map(this::mapCustomRule)
                                .toList()
                )
                .build();
    }

    private CustomRuleRequest mapCustomRule(ProductCreateRequestDto.CustomRuleRequest customRuleRequest) {
        return CustomRuleRequest
                .builder()
                .customRuleName(customRuleRequest.getCustomRuleName())
                .customRuleType(customRuleRequest.getCustomRuleType())
                .maxSelection(customRuleRequest.getMaxSelection())
                .minSelection(customRuleRequest.getMinSelection())
                .orderIndex(customRuleRequest.getOrderIndex())
                .optionRequests(
                        customRuleRequest.getOptionRequests().stream()
                                .map(this::mapOption)
                                .toList()
                )
                .build();
    }
    private OptionRequest mapOption(ProductCreateRequestDto.OptionRequest optionRequest) {
        return OptionRequest
                .builder()
                .countType(optionRequest.getCountType())
                .defaultQuantity(optionRequest.getDefaultQuantity())
                .extraPrice(optionRequest.getExtraPrice())
                .isDefault(optionRequest.getIsDefault())
                .maxQuantity(optionRequest.getMaxQuantity())
                .measureType(optionRequest.getMeasureType())
                .optionId(optionRequest.getOptionId())
                .orderIndex(optionRequest.getOrderIndex())
                .quantityDetails(optionRequest.getQuantityDetails())
                .optionTraitRequests(
                        optionRequest.getOptionTraitRequests().stream()
                                .map(this::mapOptionTrait)
                                .toList()
                )
                .build();
    }
    private OptionTraitRequest mapOptionTrait(ProductCreateRequestDto.OptionTraitRequest optionTraitRequest) {
        return OptionTraitRequest
                .builder()
                .defaultSelection(optionTraitRequest.getDefaultSelection())
                .extraCalories(optionTraitRequest.getExtraCalories())
                .extraPrice(optionTraitRequest.getExtraPrice())
                .optionTraitId(optionTraitRequest.getOptionTraitId())
                .build();
    }
}
