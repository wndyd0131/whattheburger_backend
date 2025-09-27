package com.whattheburger.backend.controller;

import com.whattheburger.backend.controller.dto.*;
import com.whattheburger.backend.controller.dto.product.ProductCreateRequestDto;
import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.dto_mapper.ProductDtoMapper;
import com.whattheburger.backend.service.S3Service;
import com.whattheburger.backend.service.dto.product.ProductCreateDto;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product")
public class ProductController {

    private final ProductService productService;
    private final ProductDtoMapper productDtoMapper;

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

    @PostMapping("/api/v1/products")
    public ResponseEntity<String> createProduct(
            @RequestPart("productBlob") ProductCreateRequestDto productCreateRequestDTO,
            @RequestPart("productImage") MultipartFile productImage
    ) throws IOException {

        log.info("Product Image {}", productImage.getOriginalFilename());

        ProductCreateDto productDto = productDtoMapper.fromControllerDto(productCreateRequestDTO);

        productService.createProduct(productDto, productImage);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Product successfully created");
    }

    @GetMapping
    public void getProductByIds(ProductReadByProductIdsRequestDto productRequestDto) {
        List<Long> productIds = productRequestDto.getProductIds();
    }
}
