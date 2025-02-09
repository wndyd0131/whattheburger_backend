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
import com.whataburger.whataburgerproject.service.dto.ProductOptionDto;
import com.whataburger.whataburgerproject.service.dto.ProductReadByCategoryIdDto;
import com.whataburger.whataburgerproject.service.exception.CategoryNotFoundException;
import com.whataburger.whataburgerproject.service.exception.OptionNotFoundException;
import com.whataburger.whataburgerproject.service.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

        Long categoryId = productCreateRequestDTO.getCategoryId();
        Category category = categoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));
        category.getProducts().add(product);
        categoryRepository.save(category);

        for (ProductCreateRequestDTO.OptionRequest optionRequest : productCreateRequestDTO.getOptions()) {
            Long optionId = optionRequest.getOptionId();
            Option option = optionRepository
                    .findById(optionId)
                    .orElseThrow(() -> new OptionNotFoundException(optionId));

            ProductOption productOption = new ProductOption(
                    savedProduct,
                    option,
                    optionRequest.getIsDefault(),
                    optionRequest.getDefaultQuantity(),
                    optionRequest.getMaxQuantity(),
                    optionRequest.getExtraPrice()
            );
            productOptionRepository.save(productOption);
        }
        return savedProduct;
    }
    public List<Product> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products;
    }

    public List<ProductReadByCategoryIdDto> findProductsByCategoryId(Long categoryId) {
        Category category = categoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new RuntimeException());
        List<Product> products = category.getProducts();
        List<ProductReadByCategoryIdDto> productReadDtoList = new ArrayList<>();
        for (Product product : products) {
            productReadDtoList.add(
                    new ProductReadByCategoryIdDto(
                            product.getId(),
                            product.getName(),
                            product.getPrice(),
                            product.getImageSource(),
                            product.getIngredientInfo()
                    )
            );
        }
        return productReadDtoList;
    }

    public Product findProductById(Long productId) {
        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        return product;
    }

    public List<ProductOption> findProductOptionByProductId(Long productId) {
        List<ProductOption> productOptions = productOptionRepository.findByProductId(productId);
        return productOptions;
    }
}