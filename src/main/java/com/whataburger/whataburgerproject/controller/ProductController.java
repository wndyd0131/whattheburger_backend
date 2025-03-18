package com.whataburger.whataburgerproject.controller;

import com.whataburger.whataburgerproject.controller.dto.*;
import com.whataburger.whataburgerproject.domain.*;
import com.whataburger.whataburgerproject.service.ProductOptionService;
import com.whataburger.whataburgerproject.service.ProductService;
import com.whataburger.whataburgerproject.service.dto.ProductReadByCategoryIdDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

    @Operation(summary = "Get product by ID", description = "Returns a product with its options and traits")
    @ApiResponse(responseCode = "200", description = "Find Product By ProductId",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ProductReadByProductIdResponseDto.class),
                    examples = @ExampleObject(value = """
                            {
                              "productId": 1,
                              "productName": "Whataburger",
                              "productPrice": 5.49,
                              "imageSource": null,
                              "briefInfo": "Large Bun (5\\\"), Large Beef Patty (5\\\") (1), Tomato (Regular), Lettuce (Regular), Pickles (Regular), Diced Onions (Regular), Mustard (Regular)",
                              "optionResponses": [
                                {
                                  "optionId": 1,
                                  "name": "large_bun",
                                  "isDefault": true,
                                  "defaultQuantity": 1,
                                  "maxQuantity": 1,
                                  "extraPrice": 0,
                                  "calories": 310,
                                  "imageSource": null,
                                  "orderIndex": 0,
                                  "customRuleResponse": {
                                    "customRuleId": 1,
                                    "name": "BREAD",
                                    "customRuleType": "UNIQUE",
                                    "rowIndex": 0,
                                    "minSelection": 1,
                                    "maxSelection": 1
                                  },
                                  "optionTraitResponses": [
                                    {
                                        "optionTraitId": 1,
                                        "name": "TBS"
                                    }
                                  ]
                                }
                              ]
                            }
                            """)
            )
    )
    @GetMapping("/api/v1/products/{productId}")
    public ProductReadByProductIdResponseDto getProductById(@PathVariable("productId") Long productId) {
        Product product = productService.findProductById(productId);
        List<ProductOption> productOptions = productOptionService.findProductOptionByProductId(product.getId());
        List<ProductReadByProductIdResponseDto.OptionResponse> optionResponses = new ArrayList<>();

        for (ProductOption productOption : productOptions) {
            Option option = productOption.getOption();
            List<ProductOptionTrait> productOptionTraits = productOption.getProductOptionTraits();
            List<ProductReadByProductIdResponseDto.OptionTraitResponse> optionTraitResponses = new ArrayList<>();
            CustomRule customRule = productOption.getCustomRule();
            ProductReadByProductIdResponseDto.CustomRuleResponse customRuleResponse =
                    ProductReadByProductIdResponseDto.CustomRuleResponse
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
                optionTraitResponses.add(
                        ProductReadByProductIdResponseDto.OptionTraitResponse
                                .builder()
                                .optionTraitId(optionTrait.getId())
                                .name(optionTrait.getName())
                                .defaultSelection(productOptionTrait.getDefaultSelection())
                                .extraPrice(productOptionTrait.getExtraPrice())
                                .extraCalories(productOptionTrait.getExtraCalories())
                                .build()
                );
            }
            optionResponses.add(ProductReadByProductIdResponseDto.OptionResponse
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
                    .customRuleResponse(customRuleResponse)
                    .optionTraitResponses(
                            optionTraitResponses
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
                optionResponses
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
