package com.whattheburger.backend.service;

import com.whattheburger.backend.controller.dto.ProductCreateRequestDto;
import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.repository.*;
import com.whattheburger.backend.service.dto.ProductReadByCategoryIdResponseDto;
import com.whattheburger.backend.service.dto.ProductReadByProductIdDto;
import com.whattheburger.backend.service.exception.CategoryNotFoundException;
import com.whattheburger.backend.service.exception.OptionNotFoundException;
import com.whattheburger.backend.service.exception.OptionTraitNotFoundException;
import com.whattheburger.backend.service.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
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
    private final CategoryProductRepository categoryProductRepository;
    private final CustomRuleRepository customRuleRepository;
    private final OptionTraitRepository optionTraitRepository;
    private final ProductOptionTraitRepository productOptionTraitRepository;

    @Transactional
    public Product createProduct(ProductCreateRequestDto productCreateRequestDTO) {
        Product product = productCreateRequestDTO.toEntity();
        Product newProduct = productRepository.save(product);
        List<Long> categoryIds = productCreateRequestDTO.getCategoryIds();
        List<ProductCreateRequestDto.CustomRuleRequest> customRuleRequests = productCreateRequestDTO.getCustomRuleRequests();

        saveCategoryProduct(categoryIds, newProduct);
        saveProductDetail(customRuleRequests, newProduct);

        return newProduct;
    }

    public List<Product> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products;
    }

    public List<ProductReadByCategoryIdResponseDto> getProductsByCategoryId(Long categoryId) {
        List<CategoryProduct> categoryProducts = categoryProductRepository.findByCategoryId(categoryId);
        List<Product> products = new ArrayList<>();
        for (CategoryProduct categoryProduct : categoryProducts) {
            products.add(categoryProduct.getProduct());
        }
        List<ProductReadByCategoryIdResponseDto> productReadDtoList = new ArrayList<>();
        for (Product product : products) {
            productReadDtoList.add(
                    new ProductReadByCategoryIdResponseDto(
                            product.getId(),
                            product.getName(),
                            product.getPrice(),
                            product.getImageSource(),
                            product.getBriefInfo()
                    )
            );
        }
        return productReadDtoList;
    }

    public ProductReadByProductIdDto getProductById(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException(productId));
        return ProductReadByProductIdDto.toDto(product);
    }

    private void saveCategoryProduct(List<Long> categoryIds, Product product) {
        for (Long categoryId : categoryIds) {
            Category category = categoryRepository
                    .findById(categoryId)
                    .orElseThrow(() -> new CategoryNotFoundException(categoryId));
            CategoryProduct categoryProduct = new CategoryProduct(category, product);
            categoryProductRepository.save(categoryProduct);
        }
    }

    private void saveProductDetail(List<ProductCreateRequestDto.CustomRuleRequest> customRuleRequests, Product newProduct) {
        for (ProductCreateRequestDto.CustomRuleRequest customRuleRequest : customRuleRequests) {
            CustomRule newCustomRule = saveCustomRule(customRuleRequest);
            for (ProductCreateRequestDto.OptionRequest optionRequest : customRuleRequest.getOptionRequests()) {
                Long optionId = optionRequest.getOptionId();
                Option option = optionRepository
                        .findById(optionId)
                        .orElseThrow(() -> new OptionNotFoundException(optionId));
                ProductOption newProductOption = saveOption(newProduct, newCustomRule, optionRequest, option);
                for (ProductCreateRequestDto.OptionTraitRequest optionTraitRequest : optionRequest.getOptionTraitRequests()) {
                    Long optionTraitId = optionTraitRequest.getOptionTraitId();
                    OptionTrait optionTrait = optionTraitRepository
                            .findById(optionTraitId)
                            .orElseThrow(() -> new OptionTraitNotFoundException(optionTraitId));
                    saveOptionTrait(newProductOption, optionTraitRequest, optionTrait);
                }
            }
        }
    }

    private CustomRule saveCustomRule(ProductCreateRequestDto.CustomRuleRequest customRuleRequest) {
        CustomRule customRule = new CustomRule(
                customRuleRequest.getCustomRuleName(),
                customRuleRequest.getCustomRuleType(),
                customRuleRequest.getOrderIndex(),
                customRuleRequest.getMinSelection(),
                customRuleRequest.getMaxSelection()
        );
        return customRuleRepository.save(customRule);
    }

    private ProductOption saveOption(Product newProduct, CustomRule newCustomRule, ProductCreateRequestDto.OptionRequest optionRequest, Option option) {
        ProductOption productOption = new ProductOption(
                newProduct,
                option,
                newCustomRule,
                optionRequest.getIsDefault(),
                optionRequest.getCountType(),
                optionRequest.getMeasureType(),
                optionRequest.getDefaultQuantity(),
                optionRequest.getMaxQuantity(),
                optionRequest.getExtraPrice(),
                optionRequest.getOrderIndex()
        );
        return productOptionRepository.save(productOption);
    }

    private void saveOptionTrait(ProductOption newProductOption, ProductCreateRequestDto.OptionTraitRequest optionTraitRequest, OptionTrait optionTrait) {
        ProductOptionTrait productOptionTrait = new ProductOptionTrait(
                newProductOption,
                optionTrait,
                optionTraitRequest.getDefaultSelection(),
                optionTraitRequest.getExtraPrice(),
                optionTraitRequest.getExtraCalories()
        );
        productOptionTraitRepository.save(productOptionTrait);
    }

}