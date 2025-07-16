package com.whattheburger.backend.service;

import com.amazonaws.services.s3.model.S3Object;
import com.whattheburger.backend.controller.dto.product.ProductCreateRequestDto;
import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.repository.*;
import com.whattheburger.backend.service.dto.product.CustomRuleRequest;
import com.whattheburger.backend.service.dto.product.OptionRequest;
import com.whattheburger.backend.service.dto.product.OptionTraitRequest;
import com.whattheburger.backend.service.dto.product.ProductCreateDto;
import com.whattheburger.backend.service.dto.ProductReadByCategoryIdResponseDto;
import com.whattheburger.backend.service.dto.ProductReadByProductIdDto;
import com.whattheburger.backend.service.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    private final ProductRepository productRepository;
    private final OptionRepository optionRepository;
    private final ProductOptionRepository productOptionRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryProductRepository categoryProductRepository;
    private final CustomRuleRepository customRuleRepository;
    private final OptionTraitRepository optionTraitRepository;
    private final ProductOptionTraitRepository productOptionTraitRepository;
    private final OptionQuantityRepository optionQuantityRepository;
    private final ProductOptionOptionQuantityRepository productOptionOptionQuantityRepository;

    private final S3Service s3Service;

    @Transactional
    public Product createProduct(ProductCreateDto productDto, MultipartFile imageSource) throws IOException {
        String s3Key = s3Service.uploadFile(imageSource); // upload product image to S3

        Product product = productDto.toEntity();
        product.changeImageSource(s3Key);
        Product newProduct = productRepository.save(product);
        List<Long> categoryIds = productDto.getCategoryIds();
        List<CustomRuleRequest> customRuleRequests = productDto.getCustomRuleRequests();

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
                            product.getCalories(),
                            product.getImageSource(),
                            product.getBriefInfo()
                    )
            );
        }
        return productReadDtoList;
    }

    public ProductReadByProductIdDto getProductById(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException(productId));
        String publicUrl = null;
        try {
            publicUrl = s3Service.getPublicUrl(product.getImageSource());
        } catch (Exception e) {
            log.error("Failed to load file from S3 {}", e.getMessage(), e);
        }
        return ProductReadByProductIdDto.toDto(product, publicUrl);
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

    private void saveProductDetail(List<CustomRuleRequest> customRuleRequests, Product newProduct) {
        for (CustomRuleRequest customRuleRequest : customRuleRequests) {
            CustomRule newCustomRule = saveCustomRule(customRuleRequest);
            for (OptionRequest optionRequest : customRuleRequest.getOptionRequests()) {
                Long optionId = optionRequest.getOptionId();
                Option option = optionRepository
                        .findById(optionId)
                        .orElseThrow(() -> new OptionNotFoundException(optionId));
                ProductOption newProductOption = saveOption(newProduct, newCustomRule, optionRequest, option);
                for (OptionTraitRequest optionTraitRequest : optionRequest.getOptionTraitRequests()) {
                    Long optionTraitId = optionTraitRequest.getOptionTraitId();
                    OptionTrait optionTrait = optionTraitRepository
                            .findById(optionTraitId)
                            .orElseThrow(() -> new OptionTraitNotFoundException(optionTraitId));
                    saveOptionTrait(newProductOption, optionTraitRequest, optionTrait);
                }
            }
        }
    }

    private CustomRule saveCustomRule(CustomRuleRequest customRuleRequest) {
        CustomRule customRule = new CustomRule(
                customRuleRequest.getCustomRuleName(),
                customRuleRequest.getCustomRuleType(),
                customRuleRequest.getOrderIndex(),
                customRuleRequest.getMinSelection(),
                customRuleRequest.getMaxSelection()
        );
        return customRuleRepository.save(customRule);
    }

    private ProductOption saveOption(Product newProduct, CustomRule newCustomRule, OptionRequest optionRequest, Option option) {
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

        ProductOption savedProductOption = productOptionRepository.save(productOption);

        optionRequest.getQuantityDetails().stream().forEach(quantityDetail -> {
            log.info("Quantity id {}", quantityDetail.getId());
            OptionQuantity optionQuantity = optionQuantityRepository.findById(quantityDetail.getId())
                    .orElseThrow(() -> new OptionQuantityNotFoundException(quantityDetail.getId()));
            ProductOptionOptionQuantity productOptionOptionQuantity = new ProductOptionOptionQuantity(
                    savedProductOption,
                    optionQuantity,
                    quantityDetail.getExtraPrice(),
                    quantityDetail.getIsDefault()
            );
            productOptionOptionQuantityRepository.save(productOptionOptionQuantity);
        });
        return savedProductOption;
    }

    private void saveOptionTrait(ProductOption newProductOption, OptionTraitRequest optionTraitRequest, OptionTrait optionTrait) {
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