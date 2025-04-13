package com.whataburger.whataburgerproject.controller;

import com.whataburger.whataburgerproject.controller.dto.*;
import com.whataburger.whataburgerproject.domain.*;
import com.whataburger.whataburgerproject.service.ProductOptionService;
import com.whataburger.whataburgerproject.service.ProductService;
import com.whataburger.whataburgerproject.service.dto.ProductReadByCategoryIdResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        ProductReadByProductIdResponseDto productResponseDto = productService.getProductById(productId);
        return productResponseDto;
    }

    @GetMapping("/api/v1/products/category/{categoryId}")
    public List<ProductReadByCategoryIdResponseDto> getProductsByCategoryId(@PathVariable("categoryId") Long categoryId) {
        List<ProductReadByCategoryIdResponseDto> productResponseDto = productService.findProductsByCategoryId(categoryId);
        return productResponseDto;
    }

    @Transactional
    @PostMapping("/api/v1/products")
    public ResponseEntity<String> createProduct(@RequestBody ProductCreateRequestDto productCreateRequestDTO) {
        productService.createProduct(productCreateRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body("Product successfully created");
    }
}
