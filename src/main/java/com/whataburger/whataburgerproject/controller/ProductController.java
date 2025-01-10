package com.whataburger.whataburgerproject.controller;

import com.whataburger.whataburgerproject.controller.dto.ProductCreateRequestDTO;
import com.whataburger.whataburgerproject.controller.dto.ProductCreateResponseDTO;
import com.whataburger.whataburgerproject.domain.Product;
import com.whataburger.whataburgerproject.service.ProductService;
import com.whataburger.whataburgerproject.service.dto.ProductCreateDetailsDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;
    @GetMapping("/api/v1/products")
    public List<Product> getAllProducts() {
        List<Product> allProducts = productService.getAllProducts();
        return allProducts;
    }

    @PostMapping("/api/v1/products")
    public ProductCreateResponseDTO createProduct(@RequestBody ProductCreateRequestDTO productCreateRequestDTO) {
        Product product = productService.createProduct(productCreateRequestDTO);
        return new ProductCreateResponseDTO(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getIngredientInfo(),
                product.getImageSource()
        );
    }

    @GetMapping("/api/v1/products/{id}")
    public Product getProductById(@PathVariable Long product_id) {
        Product product = productService.getProductById(product_id);
        return product;
    }

    @Data
    @RequiredArgsConstructor
    public class RequestProductById {
        private Long id;
    }
}
