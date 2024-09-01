package com.whataburger.whataburgerproject.service;

import com.whataburger.whataburgerproject.domain.Product;
import com.whataburger.whataburgerproject.repository.ProductRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class) // allow mocking
public class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;


    @Test
    public void ProductService_CreateProduct_ReturnsProductId() {
        Product product = new Product("burger", 5.99, "This is burger");
        product.setId(1L);
        when(productRepository.save(Mockito.any(Product.class))).thenReturn(product); // not working well..
        Long productId = productService.createProduct(product);
        System.out.println(productId);
        Assertions.assertThat(productId).isEqualTo(product.getId());
    }
    @Test
    public void ProductService_successfully_find_product_by_id() throws Exception {
        //given
        Product product = new Product("whataburger", 5.99, "This is whataburger");
        Product newProduct = productRepository.save(product);
        //then
        Product foundProduct = productService.findProductById(newProduct.getId());
        Assertions.assertThat(foundProduct.getId()).isEqualTo(newProduct.getId());
    }
}
