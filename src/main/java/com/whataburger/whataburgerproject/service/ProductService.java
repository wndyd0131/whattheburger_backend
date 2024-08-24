package com.whataburger.whataburgerproject.service;

import com.whataburger.whataburgerproject.domain.Product;
import com.whataburger.whataburgerproject.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public List<Product> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products;
    }
}
