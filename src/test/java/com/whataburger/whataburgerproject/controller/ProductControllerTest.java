package com.whataburger.whataburgerproject.controller;

import com.whataburger.whataburgerproject.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

//@WebMvcTest(ProductController.class)
public class ProductControllerTest {
    @Autowired MockMvc mockMvc;

    @MockBean // Load mocked bean (that ProductController depends on)
    ProductService productService;

    @Test
    void getProducts() throws Exception {

    }
}
