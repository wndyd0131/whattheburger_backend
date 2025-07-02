package com.whattheburger.backend.controller;

import com.whattheburger.backend.controller.dto.*;
import com.whattheburger.backend.controller.dto.product.ProductCreateRequestDto;
import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.service.dto.ProductReadByProductIdDto;
import com.whattheburger.backend.service.ProductService;
import com.whattheburger.backend.service.dto.ProductReadByCategoryIdResponseDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/api/v1/products")
    public ResponseEntity<List<ProductReadResponseDto>> getAllProducts() {
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
        return ResponseEntity.ok(productReadResponseDtoList);
    }

    @GetMapping("/api/v1/products/{productId}")
    public ResponseEntity<ProductReadByProductIdDto> getProductById(@PathVariable("productId") Long productId) {
        ProductReadByProductIdDto productDto = productService.getProductById(productId);

        return ResponseEntity.ok(productDto);
    }

    @GetMapping("/api/v1/products/category/{categoryId}")
    public ResponseEntity<List<ProductReadByCategoryIdResponseDto>> getProductsByCategoryId(@PathVariable("categoryId") Long categoryId) {
        List<ProductReadByCategoryIdResponseDto> productResponseDto = productService.getProductsByCategoryId(categoryId);

        return ResponseEntity.ok(productResponseDto);
    }

    @Transactional
    @PostMapping("/api/v1/products")
    public ResponseEntity<String> createProduct(
            @RequestPart("productBlob") ProductCreateRequestDto productCreateRequestDTO,
            @RequestPart("productImage") MultipartFile productImage
    ) {

        log.info("Product Image {}", productImage.getOriginalFilename());
        productService.createProduct(productCreateRequestDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Product successfully created");
    }

    @GetMapping
    public void getProductByIds(ProductReadByProductIdsRequestDto productRequestDto) {
        List<Long> productIds = productRequestDto.getProductIds();
    }
}
