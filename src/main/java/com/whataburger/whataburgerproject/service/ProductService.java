package com.whataburger.whataburgerproject.service;

import com.whataburger.whataburgerproject.controller.dto.ProductCreateRequestDTO;
import com.whataburger.whataburgerproject.domain.Ingredient;
import com.whataburger.whataburgerproject.domain.Product;
import com.whataburger.whataburgerproject.domain.temp.ProductIngredient;
import com.whataburger.whataburgerproject.repository.IngredientRepository;
import com.whataburger.whataburgerproject.repository.ProductIngredientRepository;
import com.whataburger.whataburgerproject.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;
    private final ProductIngredientRepository productIngredientRepository;

    public Product createProduct(ProductCreateRequestDTO productCreateRequestDTO) {
        Product product = productCreateRequestDTO.toEntity();
        Product savedProduct = productRepository.save(product);
        for (ProductCreateRequestDTO.IngredientRequest ir : productCreateRequestDTO.getIngredients()) {
            Long ingredientId = ir.getIngredientId();
            Ingredient ingredient = ingredientRepository
                    .findById(ingredientId)
                    .orElseThrow(() -> new RuntimeException());

            ProductIngredient productIngredient = new ProductIngredient(
                    savedProduct,
                    ingredient
            );
            productIngredientRepository.save(productIngredient);
        }
        return savedProduct;
    }
    public List<Product> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products;
    }

    public Product getProductById(Long productId) {
        Product product = productRepository.findById(productId).get();
        return product;
    }

}
