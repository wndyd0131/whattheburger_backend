//package com.whataburger.whataburgerproject.service;
//
//import com.whataburger.whataburgerproject.controller.dto.ProductCreateRequestDto;
//import com.whataburger.whataburgerproject.domain.*;
//import com.whataburger.whataburgerproject.domain.enums.CustomRuleType;
//import com.whataburger.whataburgerproject.domain.enums.MeasureType;
//import com.whataburger.whataburgerproject.domain.enums.OptionTraitType;
//import com.whataburger.whataburgerproject.domain.enums.ProductType;
//import com.whataburger.whataburgerproject.repository.*;
//import org.assertj.core.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Arrays;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.*;
//
////@SpringBootTest
//public class ProductServiceProductControllerIntegrationTest {
//    @Autowired
//    ProductRepository productRepository;
//    @Autowired
//    OptionRepository optionRepository;
//    @Autowired
//    ProductOptionRepository productOptionRepository;
//    @Autowired
//    CategoryRepository categoryRepository;
//    @Autowired
//    CustomRuleRepository customRuleRepository;
//    @Autowired
//    OptionTraitRepository optionTraitRepository;
//    @Autowired
//    ProductOptionTraitRepository productOptionTraitRepository;
//    @Autowired
//    ProductService productService;
//
//    private Category category;
//    private OptionTrait optionTrait;
//    private Option option;
//
//    private List<ProductCreateRequestDto.CustomRuleRequest> buildValidCustomRuleRequests() {
//        List<ProductCreateRequestDto.OptionTraitRequest> optionTraitRequests = Arrays.asList(
//                ProductCreateRequestDto.OptionTraitRequest
//                        .builder()
//                        .optionTraitId(optionTrait.getId())
//                        .defaultSelection(0)
//                        .extraCalories(0D)
//                        .extraPrice(1D)
//                        .build()
//        );
//
//
//        List<ProductCreateRequestDto.OptionRequest> optionRequests = Arrays.asList(
//                ProductCreateRequestDto.OptionRequest
//                        .builder()
//                        .optionId(option.getId())
//                        .isDefault(true)
//                        .measureType(MeasureType.COUNT)
//                        .defaultQuantity(1)
//                        .maxQuantity(1)
//                        .extraPrice(0D)
//                        .orderIndex(0)
//                        .optionTraitRequests(optionTraitRequests)
//                        .build()
//        );
//
//        return Arrays.asList(
//                ProductCreateRequestDto.CustomRuleRequest
//                        .builder()
//                        .customRuleName("Bread")
//                        .customRuleType(CustomRuleType.UNIQUE)
//                        .minSelection(1)
//                        .maxSelection(1)
//                        .orderIndex(0)
//                        .optionRequests(optionRequests)
//                        .build()
//        );
//    }
//
//    @BeforeEach
//    void setUp() {
//        category = categoryRepository.save(new Category("Burgers"));
//        option = optionRepository.save(new Option("Large Bun", "large_bun.jpg", 310D));
//        optionTrait = optionTraitRepository.save(new OptionTrait("Toast Both Sides", "TBS", OptionTraitType.BINARY));
//    }
//
//    @Test
//    @Transactional
//    void createProduct_success_withValidInput() {
//        String productName = "Whataburger";
//        double productPrice = 5.99;
//        double calories = 580;
//        ProductType productType = ProductType.ONLY;
//        String briefInfo = "Whataburger consists of a bread, beef, toppings, and sauce.";
//
//        List<ProductCreateRequestDto.CustomRuleRequest> customRuleRequests = buildValidCustomRuleRequests();
//
//        ProductCreateRequestDto createRequestDto = new ProductCreateRequestDto(
//                productName,
//                productPrice,
//                calories,
//                productType,
//                briefInfo,
//                "Whataburger.jpg",
//                category.getId(),
//                customRuleRequests
//        );
//
//        Product product = productService.createProduct(createRequestDto);
//
//        assertThat(product).isNotNull();
//        assertThat(product.getName()).isEqualTo("Whataburger");
//        assertThat(product.getPrice()).isEqualTo(5.99);
//        assertThat(product.getCalories()).isEqualTo(580);
//        assertThat(product.getProductType()).isEqualTo(ProductType.ONLY);
//        assertThat(product.getCategories()).contains(category);
//        assertThat(product.getProductOptions()).isNotNull();
//    }
//
//    @Test
//    @Transactional
//    void createProduct_fail_whenProductNameIsInvalid() {
//        String productName = "";
//        double productPrice = 5.99;
//        double calories = 580;
//        ProductType productType = ProductType.ONLY;
//        String briefInfo = "Whataburger consists of a bread, beef, toppings, and sauce.";
//
//        List<ProductCreateRequestDto.CustomRuleRequest> customRuleRequests = buildValidCustomRuleRequests();
//
//        ProductCreateRequestDto createRequestDto = new ProductCreateRequestDto(
//                productName,
//                productPrice,
//                calories,
//                productType,
//                briefInfo,
//                "Whataburger.jpg",
//                category.getId(),
//                customRuleRequests
//        );
//
////        Assertions.assertThatThrownBy(() -> productService.createProduct(createRequestDto)).isInstanceOf();
//    }
//}
