package com.whattheburger.backend.integration;


import com.whattheburger.backend.controller.ProductController;
import com.whattheburger.backend.controller.dto.product.ProductCreateRequestDto;
import com.whattheburger.backend.domain.Category;
import com.whattheburger.backend.domain.Option;
import com.whattheburger.backend.domain.OptionTrait;
import com.whattheburger.backend.domain.enums.CustomRuleType;
import com.whattheburger.backend.domain.enums.MeasureType;
import com.whattheburger.backend.domain.enums.OptionTraitType;
import com.whattheburger.backend.domain.enums.ProductType;
import com.whattheburger.backend.repository.*;
import com.whattheburger.backend.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")

public class ProductControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
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
    @Autowired
    ProductController productController;

    Category category;
    OptionTrait optionTrait;
    Option option;

    @BeforeEach
    void setUp() {
        category = categoryRepository.save(new Category("Burgers"));
        option = optionRepository.save(new Option("Large Bun", "/img/large_bun.jpg", 310D));
        optionTrait = optionTraitRepository.save(new OptionTrait("Toast Both Sides", "TBS", OptionTraitType.BINARY));
    }

    private List<ProductCreateRequestDto.CustomRuleRequest> buildValidCustomRuleRequests() {
        List<ProductCreateRequestDto.OptionTraitRequest> optionTraitRequests = Arrays.asList(
                ProductCreateRequestDto.OptionTraitRequest
                        .builder()
                        .optionTraitId(optionTrait.getId())
                        .defaultSelection(0)
                        .extraCalories(0D)
                        .extraPrice(BigDecimal.valueOf(1))
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
                        .extraPrice(BigDecimal.ZERO)
                        .orderIndex(0)
                        .optionTraitRequests(optionTraitRequests)
                        .build()
        );

        return Arrays.asList(
                ProductCreateRequestDto.CustomRuleRequest
                        .builder()
                        .customRuleName("Bread")
                        .customRuleType(CustomRuleType.UNIQUE)
                        .minSelection(1)
                        .maxSelection(1)
                        .orderIndex(0)
                        .optionRequests(optionRequests)
                        .build()
        );
    }

    @Test
    public void givenProduct_whenCreateProduct_thenStatus201() throws Exception {
        String productName = "Whattheburger";
        Double productPrice = 5.99;
        Double productCalories = 590D;
        ProductType productType = ProductType.ONLY;
        String briefInfo = "Lorem ipsum dolor sit amet, consectetur adipisicing elit. Vitae fugiat dolore aperiam nesciunt dolores iure, aliquam suscipit blanditiis enim cum ut laboriosam quibusdam veniam assumenda expedita, ea sapiente sunt nam.";
        String imageSource = "/img/whattheburger.jpg";
        List<Long> categoryIds = Arrays.asList(1L);
        List<ProductCreateRequestDto.CustomRuleRequest> customRuleRequests = buildValidCustomRuleRequests();

//        ResponseEntity<String> responseEntity = productController.createProduct(
//                ProductCreateRequestDto
//                        .builder()
//                        .productName(productName)
//                        .productPrice(productPrice)
//                        .productCalories(productCalories)
//                        .productType(productType)
//                        .briefInfo(briefInfo)
//                        .imageSource(imageSource)
//                        .categoryIds(categoryIds)
//                        .customRuleRequests(customRuleRequests)
//                        .build()
//        );
//
//        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }
}
