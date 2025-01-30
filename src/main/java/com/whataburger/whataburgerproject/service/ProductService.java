package com.whataburger.whataburgerproject.service;

import com.whataburger.whataburgerproject.controller.dto.ProductCreateRequestDTO;
import com.whataburger.whataburgerproject.domain.Category;
import com.whataburger.whataburgerproject.domain.Option;
import com.whataburger.whataburgerproject.domain.Product;
import com.whataburger.whataburgerproject.domain.ProductOption;
import com.whataburger.whataburgerproject.repository.CategoryRepository;
import com.whataburger.whataburgerproject.repository.OptionRepository;
import com.whataburger.whataburgerproject.repository.ProductOptionRepository;
import com.whataburger.whataburgerproject.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final OptionRepository optionRepository;
    private final ProductOptionRepository productOptionRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public Product createProduct(ProductCreateRequestDTO productCreateRequestDTO) {
        Product product = productCreateRequestDTO.toEntity();
        Product savedProduct = productRepository.save(product);

        Category category = categoryRepository.findById(productCreateRequestDTO
                        .getCategoryId())
                .orElseThrow(() -> new RuntimeException());
        category.getProducts().add(product);
        categoryRepository.save(category);

        for (ProductCreateRequestDTO.OptionRequest optionRequest : productCreateRequestDTO.getOptions()) {
            Long optionId = optionRequest.getOptionId();
            Boolean isDefault = optionRequest.getIsDefault();
            int defaultQuantity = optionRequest.getDefaultQuantity();
            Option option = optionRepository
                    .findById(optionId)
                    .orElseThrow(() -> new RuntimeException());

            ProductOption productOption = new ProductOption(
                    savedProduct,
                    option,
                    isDefault,
                    defaultQuantity
            );
            productOptionRepository.save(productOption);
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
