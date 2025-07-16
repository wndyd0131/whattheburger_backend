package com.whattheburger.backend.controller;

import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.domain.enums.*;
import com.whattheburger.backend.security.JwtFilter;
import com.whattheburger.backend.service.ProductService;
import com.whattheburger.backend.service.dto.ProductReadByProductIdDto;
import com.whattheburger.backend.util.JwtTokenUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Controller requires Application Context
@WebMvcTest(
        controllers = ProductController.class,
        excludeAutoConfiguration = SecurityAutoConfiguration.class
)
public class ProductControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    JwtFilter jwtFilter;
    @MockBean
    JwtTokenUtil jwtTokenUtil;
    @MockBean
    ProductService productService;

    @Test
    void whenGetProductById_thenOK() throws Exception {

        CustomRule customRule = CustomRule
                .builder()
                .id(1L)
                .name("Bread")
                .customRuleType(CustomRuleType.UNIQUE)
                .orderIndex(0)
                .minSelection(1)
                .maxSelection(1)
                .build();
        Option option = Option
                .builder()
                .id(1L)
                .name("Large Bun")
                .imageSource("img")
                .calories(310D)
                .build();
        OptionTrait optionTrait = OptionTrait
                .builder()
                .id(1L)
                .name("Toast Both Sides")
                .labelCode("TBS")
                .optionTraitType(OptionTraitType.BINARY)
                .build();
        ProductOptionTrait productOptionTrait = ProductOptionTrait
                .builder()
                .id(1L)
                .defaultSelection(1)
                .optionTrait(optionTrait)
                .extraPrice(0D)
                .extraCalories(0D)
                .build();
        ProductOption productOption = ProductOption
                .builder()
                .id(1L)
                .countType(CountType.COUNTABLE)
                .isDefault(true)
                .defaultQuantity(1)
                .maxQuantity(1)
                .extraPrice(0D)
                .measureType(MeasureType.COUNT)
                .orderIndex(0)
                .option(option)
                .customRule(customRule)
                .productOptionTraits(List.of(productOptionTrait))
                .build();
        Product product = Product
                .builder()
                .name("Whattheburger")
                .price(0D)
                .briefInfo("")
                .imageSource("")
                .calories(0D)
                .productType(ProductType.ONLY)
                .productOptions(List.of(productOption))
                .build();
        ProductReadByProductIdDto productReadByProductIdDto = ProductReadByProductIdDto.toDto(product, null);

        when(productService.getProductById(1L)).thenReturn(productReadByProductIdDto);

        mockMvc.perform(get("/api/v1/products/{productId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Whattheburger"));

        verify(productService).getProductById(1L);
    }
}
