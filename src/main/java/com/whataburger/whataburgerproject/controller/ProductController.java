package com.whataburger.whataburgerproject.controller;

import com.whataburger.whataburgerproject.domain.Product;
import com.whataburger.whataburgerproject.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    @GetMapping("/api/v1/products")
    private List<Product> getAllProducts() {
        List<Product> allProducts = productService.getAllProducts();
        return allProducts;
    }
}
