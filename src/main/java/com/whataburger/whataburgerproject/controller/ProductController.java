package com.whataburger.whataburgerproject.controller;

import com.whataburger.whataburgerproject.controller.dto.*;
import com.whataburger.whataburgerproject.domain.*;
import com.whataburger.whataburgerproject.service.ProductOptionService;
import com.whataburger.whataburgerproject.service.ProductService;
import com.whataburger.whataburgerproject.service.dto.ProductReadByCategoryIdDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product")
public class ProductController {

    private final ProductService productService;
    private final ProductOptionService productOptionService;

    @GetMapping("/api/v1/products")
    public List<ProductReadResponseDto> getAllProducts() {
        List<Product> allProducts = productService.getAllProducts();
        List<ProductReadResponseDto> productReadResponseDtoList = new ArrayList<>();
        for (Product product : allProducts) {
            productReadResponseDtoList.add(
                    new ProductReadResponseDto(
                            product.getId(),
                            product.getName(),
                            product.getPrice(),
                            product.getBriefInfo(),
                            product.getImageSource(),
                            product.getProductType()
                    )
            );
        }
        return productReadResponseDtoList;
    }

    @GetMapping("/api/v1/products/{productId}")
    public ProductReadByProductIdResponseDto getProductById(@PathVariable("productId") Long productId) {
        Product product = productService.findProductById(productId);
        List<ProductOption> productOptions = productOptionService.findProductOptionByProductId(product.getId());
        List<ProductReadByProductIdResponseDto.OptionRequest> optionRequests = new ArrayList<>();

        for (ProductOption productOption : productOptions) {
            Option option = productOption.getOption();
            List<ProductOptionTrait> productOptionTraits = productOption.getProductOptionTraits();
            List<ProductReadByProductIdResponseDto.OptionTraitRequest> optionTraitRequests = new ArrayList<>();
            CustomRule customRule = productOption.getCustomRule();
            ProductReadByProductIdResponseDto.CustomRuleRequest customRuleRequest =
                    ProductReadByProductIdResponseDto.CustomRuleRequest
                            .builder()
                            .customRuleId(customRule.getId())
                            .name(customRule.getName())
                            .customRuleType(customRule.getCustomRuleType())
                            .rowIndex(customRule.getRowIndex())
                            .minSelection(customRule.getMin_selection())
                            .maxSelection(customRule.getMax_selection())
                            .build();
            for (ProductOptionTrait productOptionTrait : productOptionTraits) {
                OptionTrait optionTrait = productOptionTrait.getOptionTrait();
                optionTraitRequests.add(
                        ProductReadByProductIdResponseDto.OptionTraitRequest
                                .builder()
                                .optionTraitId(optionTrait.getId())
                                .name(optionTrait.getName())
                                .isDefault(productOptionTrait.getIsDefault())
                                .extraPrice(productOptionTrait.getExtraPrice())
                                .extraCalories(productOptionTrait.getExtraCalories())
                                .build()
                );
            }
            optionRequests.add(ProductReadByProductIdResponseDto.OptionRequest
                    .builder()
                    .optionId(option.getId())
                    .name(option.getName())
                    .isDefault(productOption.getIsDefault())
                    .defaultQuantity(productOption.getDefaultQuantity())
                    .maxQuantity(productOption.getMaxQuantity())
                    .extraPrice(productOption.getExtraPrice())
                    .calories(option.getCalories())
                    .imageSource(option.getImageSource())
                    .orderIndex(productOption.getOrderIndex())
                    .customRuleRequest(customRuleRequest)
                    .optionTraitRequests(
                            optionTraitRequests
                    )

                    .build()
            );
        }
        return new ProductReadByProductIdResponseDto(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getImageSource(),
                product.getBriefInfo(),
                optionRequests
        );
    }

    @GetMapping("/api/v1/products/category/{categoryId}")
    public List<ProductReadByCategoryIdResponseDto> getProductsByCategoryId(@PathVariable("categoryId") Long categoryId) {
        List<ProductReadByCategoryIdDto> productsByCategoryId = productService.findProductsByCategoryId(categoryId);
        List<ProductReadByCategoryIdResponseDto> productReadResponseDto = new ArrayList<>();
        for (ProductReadByCategoryIdDto productDto : productsByCategoryId) {
            productReadResponseDto.add(
                    new ProductReadByCategoryIdResponseDto(
                            productDto.getProductId(),
                            productDto.getProductName(),
                            productDto.getProductPrice(),
                            productDto.getImageSource(),
                            productDto.getBriefInfo()
                    )
            );
        }
        return productReadResponseDto;
    }

    @Transactional
    @PostMapping("/api/v1/products")
    public ProductCreateResponseDTO createProduct(@RequestBody ProductCreateRequestDTO productCreateRequestDTO) {
        Product product = productService.createProduct(productCreateRequestDTO);
        return new ProductCreateResponseDTO(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getBriefInfo(),
                product.getImageSource()
        );
    }
}
