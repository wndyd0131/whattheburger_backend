package com.whataburger.whataburgerproject.service;

import com.whataburger.whataburgerproject.controller.dto.ProductCreateRequestDto;
import com.whataburger.whataburgerproject.domain.*;
import com.whataburger.whataburgerproject.domain.enums.CustomRuleType;
import com.whataburger.whataburgerproject.domain.enums.MeasureType;
import com.whataburger.whataburgerproject.domain.enums.ProductType;
import com.whataburger.whataburgerproject.repository.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class ProductServiceIntegrationTest {
    @Autowired
    ProductRepository productRepository;
    @Autowired
    OptionRepository optionRepository;
    @Autowired
    ProductOptionRepository productOptionRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    CustomRuleRepository customRuleRepository;
    @Autowired
    OptionTraitRepository optionTraitRepository;
    @Autowired
    ProductOptionTraitRepository productOptionTraitRepository;
    @Autowired
    ProductService productService;

    @Test
    @Transactional
    void createProduct_withValidInput_savesNewProduct() {
        String productName = "Whataburger";
        double productPrice = 5.99;
        double calories = 580;
        ProductType productType = ProductType.ONLY;
        String briefInfo = "Whataburger consists of a bread, beef, toppings, and sauce.";
        Category category = categoryRepository.save(new Category("Burgers"));
        OptionTrait optionTrait = optionTraitRepository.save(new OptionTrait("TBS"));
        Option option = optionRepository.save(new Option("Large Bun", "large_bun.jpg", 310));

        List<ProductCreateRequestDto.OptionTraitRequest> optionTraitRequests = Arrays.asList(
                ProductCreateRequestDto.OptionTraitRequest
                        .builder()
                        .optionTraitId(optionTrait.getId())
                        .defaultSelection(0)
                        .extraCalories(0)
                        .extraPrice(1)
                        .build()
        );


        List<ProductCreateRequestDto.OptionRequest> optionRequests = Arrays.asList(
                ProductCreateRequestDto.OptionRequest
                        .builder()
                        .optionId(option.getId())
                        .isDefault(true)
                        .measureType(MeasureType.COUNT)
                        .defaultQuantity(1)
                        .maxQuantity(1)
                        .extraPrice(0)
                        .orderIndex(0)
                        .optionTraitRequests(optionTraitRequests)
                        .build()
        );

        List<ProductCreateRequestDto.CustomRuleRequest> customRuleRequests = Arrays.asList(
                ProductCreateRequestDto.CustomRuleRequest
                        .builder()
                        .customRuleName("Bread")
                        .customRuleType(CustomRuleType.UNIQUE)
                        .minSelection(1)
                        .maxSelection(1)
                        .rowIndex(0)
                        .optionRequests(optionRequests)
                        .build()
        );

        ProductCreateRequestDto createRequestDto = new ProductCreateRequestDto(
                productName,
                productPrice,
                calories,
                productType,
                briefInfo,
                "Whataburger.jpg",
                category.getId(),
                customRuleRequests
        );

        Product product = productService.createProduct(createRequestDto);

        Assertions.assertThat(product).isNotNull();
    }
}
