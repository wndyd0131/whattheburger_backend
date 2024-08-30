package com.whataburger.whataburgerproject.service;

import com.whataburger.whataburgerproject.domain.Product;
import com.whataburger.whataburgerproject.repository.ProductRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    ProductRepository productRepository;
    @Autowired ProductService productService;

    @Test
    public void should_successfully_find_product_by_id() throws Exception {
        //given
        Product product = new Product("whataburger", 5.99, "This is whataburger");
        Product newProduct = productRepository.save(product);
        //then
        Product foundProduct = productService.findProductById(newProduct.getId());
        Assertions.assertThat(foundProduct.getId()).isEqualTo(newProduct.getId());
    }
}
