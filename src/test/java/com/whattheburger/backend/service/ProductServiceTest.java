package com.whattheburger.backend.service;

import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.domain.enums.*;
import com.whattheburger.backend.repository.*;
import com.whattheburger.backend.service.dto.product.CustomRuleRequest;
import com.whattheburger.backend.service.dto.product.OptionRequest;
import com.whattheburger.backend.service.dto.product.OptionTraitRequest;
import com.whattheburger.backend.service.dto.product.ProductCreateDto;
import com.whattheburger.backend.utils.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Enable Mockito Annotations
public class ProductServiceTest {

    @Mock ProductRepository productRepository;
    @Mock OptionRepository optionRepository;
    @Mock ProductOptionRepository productOptionRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock CategoryProductRepository categoryProductRepository;
    @Mock CustomRuleRepository customRuleRepository;
    @Mock OptionTraitRepository optionTraitRepository;
    @Mock ProductOptionTraitRepository productOptionTraitRepository;

    @InjectMocks
    ProductService productService;

    Category mockCategory;
    Option mockOption;
    OptionTrait mockOptionTrait;
    Product mockProduct;
    CustomRule mockCustomRule;
    CategoryProduct mockCategoryProduct;
    ProductOption mockProductOption;
    ProductOptionTrait mockProductOptionTrait;

    @BeforeEach
    void setUp() {
        initMock();
    }

    @Test
    public void givenValidRequest_whenCreateProduct_thenProductIsSavedAndReturned() throws Exception {
        Long categoryId = 1L;
        Long optionId = 1L;
        Long optionTraitId = 1L;

        Category mockCategory = new Category("Burgers");
        Option mockOption = new Option(
                "Large Bun",
                "img",
                310D
        );
        OptionTrait mockOptionTrait = new OptionTrait(
                "Toast Both Sides",
                "TBS",
                OptionTraitType.BINARY
        );
        Product mockProduct = new Product(
                "Whattheburger",
                new BigDecimal("5.99"),
                "",
                590D,
                ProductType.ONLY
        );
        CategoryProduct mockCategoryProduct = new CategoryProduct(
                mockCategory,
                mockProduct
        );
        CustomRule mockCustomRule = new CustomRule(
                "Bread",
                CustomRuleType.UNIQUE,
                0,
                1,
                1
        );
        ProductOption mockProductOption = new ProductOption(
                mockProduct,
                mockOption,
                mockCustomRule,
                Boolean.FALSE,
                CountType.COUNTABLE,
                MeasureType.COUNT,
                0,
                0,
                BigDecimal.ZERO,
                0
        );
        ProductOptionTrait mockProductOptionTrait = new ProductOptionTrait(
                mockProductOption,
                mockOptionTrait,
                0,
                BigDecimal.ZERO,
                0D
        );

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(mockCategory));
        when(optionRepository.findById(optionId)).thenReturn(Optional.of(mockOption));
        when(optionTraitRepository.findById(optionTraitId)).thenReturn(Optional.of(mockOptionTrait));
        when(productRepository.save(any(Product.class))).thenReturn(mockProduct);
        when(categoryProductRepository.save(any(CategoryProduct.class))).thenReturn(mockCategoryProduct);

        when(productOptionRepository.save(any(ProductOption.class))).thenReturn(mockProductOption);
        when(productOptionTraitRepository.save(any(ProductOptionTrait.class))).thenReturn(mockProductOptionTrait);
        when(customRuleRepository.save(any(CustomRule.class))).thenReturn(mockCustomRule);


        List<OptionTraitRequest> optionTraitRequests = Arrays.asList(OptionTraitRequest
                .builder()
                .optionTraitId(optionTraitId)
                .defaultSelection(0)
                .extraPrice(BigDecimal.ZERO)
                .extraCalories(0D)
                .build()
        );

        List<OptionRequest> optionRequests = Arrays.asList(OptionRequest
                .builder()
                .optionId(optionId)
                .isDefault(true)
                .countType(CountType.COUNTABLE)
                .measureType(MeasureType.COUNT)
                .defaultQuantity(0)
                .maxQuantity(0)
                .extraPrice(BigDecimal.ZERO)
                .orderIndex(0)
                .optionTraitRequests(optionTraitRequests)
                .build()
        );

        List<CustomRuleRequest> customRuleRequests = Arrays.asList(CustomRuleRequest
                .builder()
                .customRuleName("")
                .customRuleType(CustomRuleType.UNIQUE)
                .orderIndex(0)
                .minSelection(0)
                .maxSelection(0)
                .optionRequests(optionRequests)
                .build()
        );

        Product product = productService.createProduct(
                ProductCreateDto.
                        builder()
                        .productName("Whattheburger")
                        .productPrice(new BigDecimal("5.99"))
                        .productCalories(590D)
                        .productType(ProductType.ONLY)
                        .briefInfo("")
                        .imageSource("")
                        .categoryIds(Arrays.asList(categoryId))
                        .customRuleRequests(customRuleRequests)
                        .build()
                , null
        );

        verify(categoryRepository).findById(categoryId);
        verify(optionRepository).findById(optionId);
        verify(optionTraitRepository).findById(optionTraitId);
        verify(productRepository).save(any(Product.class));
        verify(categoryProductRepository).save(any(CategoryProduct.class));
        verify(productOptionRepository).save(any(ProductOption.class));
        verify(productOptionTraitRepository).save(any(ProductOptionTrait.class));
        verify(customRuleRepository).save(any(CustomRule.class));

        Assertions.assertThat(product).isNotNull();
        Assertions.assertThat(product.getName()).isEqualTo("Whattheburger");
    }

    @Test
    public void givenProductId_whenReadProduct_thenReturn() throws Exception {

    }

    private void initMock() {
        mockCategory = MockCategoryFactory.createMockCategory();
        mockOption = MockOptionFactory.createMockOption();
        mockOptionTrait = MockOptionTraitFactory.createMockOptionTrait();
        mockProduct = MockProductFactory.createMockProduct();
        mockCustomRule = MockCustomRuleFactory.createMockCustomRule();
        mockCategoryProduct = MockProductFactory.createMockCategoryProduct(mockCategory, mockProduct);
        mockProductOption = MockOptionFactory.createMockProductOption(mockProduct, mockOption, mockCustomRule);
        mockProductOptionTrait = MockOptionTraitFactory.createMockProductOptionTrait(mockProductOption, mockOptionTrait);
    }
}
