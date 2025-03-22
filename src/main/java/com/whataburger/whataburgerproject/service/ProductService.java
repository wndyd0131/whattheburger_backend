package com.whataburger.whataburgerproject.service;

import com.whataburger.whataburgerproject.controller.dto.ProductCreateRequestDTO;
import com.whataburger.whataburgerproject.controller.dto.ProductReadByProductIdResponseDto;
import com.whataburger.whataburgerproject.domain.*;
import com.whataburger.whataburgerproject.repository.CategoryRepository;
import com.whataburger.whataburgerproject.repository.OptionRepository;
import com.whataburger.whataburgerproject.repository.ProductOptionRepository;
import com.whataburger.whataburgerproject.repository.ProductRepository;
import com.whataburger.whataburgerproject.service.dto.ProductReadByCategoryIdResponseDto;
import com.whataburger.whataburgerproject.service.exception.CategoryNotFoundException;
import com.whataburger.whataburgerproject.service.exception.OptionNotFoundException;
import com.whataburger.whataburgerproject.service.exception.ProductNotFoundException;
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
                    optionRequest.getExtraPrice(),
                    optionRequest.getOrderIndex()
            );
            productOptionRepository.save(productOption);
        }
        return savedProduct;
    }

    public List<Product> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products;
    }

    public List<ProductReadByCategoryIdResponseDto> findProductsByCategoryId(Long categoryId) {
        Category category = categoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new RuntimeException());
        List<Product> products = category.getProducts();
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

    public ProductReadByProductIdResponseDto findProductById(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException(productId));
        List<ProductOption> productOptions = productOptionRepository.findByProductId(product.getId());
        List<ProductReadByProductIdResponseDto.OptionResponse> optionResponses = new ArrayList<>();

        for (ProductOption productOption : productOptions) {
            Option option = productOption.getOption();
            List<ProductOptionTrait> productOptionTraits = productOption.getProductOptionTraits();
            List<ProductReadByProductIdResponseDto.OptionTraitResponse> optionTraitResponses = new ArrayList<>();
            CustomRule customRule = productOption.getCustomRule();
            ProductReadByProductIdResponseDto.CustomRuleResponse customRuleResponse =
                    ProductReadByProductIdResponseDto.CustomRuleResponse
                            .builder()
                            .customRuleId(customRule.getId())
                            .name(customRule.getName())
                            .customRuleType(customRule.getCustomRuleType())
                            .rowIndex(customRule.getRowIndex())
                            .minSelection(customRule.getMin_selection())
                            .maxSelection(customRule.getMax_selection())
                            .build();
            for (ProductOptionTrait productOptionTrait : productOptionTraits) {
                OptionTrait optionTrait = productOptionTrait.getOptionTrait();
                optionTraitResponses.add(
                        ProductReadByProductIdResponseDto.OptionTraitResponse
                                .builder()
                                .optionTraitId(optionTrait.getId())
                                .name(optionTrait.getName())
                                .defaultSelection(productOptionTrait.getDefaultSelection())
                                .extraPrice(productOptionTrait.getExtraPrice())
                                .extraCalories(productOptionTrait.getExtraCalories())
                                .build()
                );
            }
            optionResponses.add(ProductReadByProductIdResponseDto.OptionResponse
                    .builder()
                    .optionId(option.getId())
                    .name(option.getName())
                    .isDefault(productOption.getIsDefault())
                    .defaultQuantity(productOption.getDefaultQuantity())
                    .maxQuantity(productOption.getMaxQuantity())
                    .extraPrice(productOption.getExtraPrice())
                    .calories(option.getCalories())
                    .imageSource(option.getImageSource())
                    .orderIndex(productOption.getOrderIndex())
                    .customRuleResponse(customRuleResponse)
                    .optionTraitResponses(
                            optionTraitResponses
                    )

                    .build()
            );
        }
        return new ProductReadByProductIdResponseDto(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getImageSource(),
                product.getBriefInfo(),
                optionResponses
        );
    }
}