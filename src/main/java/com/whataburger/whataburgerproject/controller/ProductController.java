package com.whataburger.whataburgerproject.controller;

import com.whataburger.whataburgerproject.controller.dto.ProductCreateRequestDTO;
import com.whataburger.whataburgerproject.controller.dto.ProductCreateResponseDTO;
import com.whataburger.whataburgerproject.domain.Product;
import com.whataburger.whataburgerproject.service.ProductService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    @GetMapping("/api/v1/products")
    public List<Product> getAllProducts() {
        List<Product> allProducts = productService.findAllProducts();
        return allProducts;
    }

    @PostMapping("/api/v1/products")
    public ProductCreateResponseDTO createProduct(@RequestBody ProductCreateRequestDTO productCreateRequestDTO) {
        Product product = productCreateRequestDTO.toEntity();
        System.out.println(product.getIngredientInfo());
        Long productId = productService.createProduct(product);
        return new ProductCreateResponseDTO(productId);
    }

    @GetMapping("/api/v1/products/{id}")
    public Product getProductById(@PathVariable Long product_id) {
        Product product = productService.findProductById(product_id);
        return product;
    }

    @Data
    @RequiredArgsConstructor
    public class RequestProductById {
        private Long id;
    }
}
