package com.whataburger.whataburgerproject.service;

import com.whataburger.whataburgerproject.domain.Product;
import com.whataburger.whataburgerproject.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public Long createProduct(Product product) {
        System.out.println(product.getName());
        productRepository.save(product);
        return product.getId();
    }
    public List<Product> findAllProducts() {
        List<Product> products = productRepository.findAll();
        return products;
    }

    public Product findProductById(Long productId) {
        Product product = productRepository.findById(productId).get();
        return product;
    }

}
