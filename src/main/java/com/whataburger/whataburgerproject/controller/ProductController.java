package com.whataburger.whataburgerproject.controller;

import com.whataburger.whataburgerproject.controller.dto.*;
import com.whataburger.whataburgerproject.domain.*;
import com.whataburger.whataburgerproject.service.ProductOptionService;
import com.whataburger.whataburgerproject.service.ProductService;
import com.whataburger.whataburgerproject.service.dto.ProductReadByCategoryIdResponseDto;
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

    @GetMapping("/api/v1/products/{productId}")
    public ProductReadByProductIdResponseDto getProductById(@PathVariable("productId") Long productId) {
        ProductReadByProductIdResponseDto productResponseDto = productService.getProductById(productId);
        return productResponseDto;
    }

    @GetMapping("/api/v1/products/category/{categoryId}")
    public List<ProductReadByCategoryIdResponseDto> getProductsByCategoryId(@PathVariable("categoryId") Long categoryId) {
        List<ProductReadByCategoryIdResponseDto> productResponseDto = productService.getProductsByCategoryId(categoryId);
        return productResponseDto;
    }

    @Transactional
    @PostMapping("/api/v1/products")
    public ResponseEntity<String> createProduct(@RequestBody ProductCreateRequestDto productCreateRequestDTO) {
        productService.createProduct(productCreateRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body("Product successfully created");
    }
}
