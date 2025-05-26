package com.whattheburger.backend.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

//@DataJpaTest
@ActiveProfiles("test")
public class ProductRepositoryTest {
    @Autowired
    ProductRepository productRepository;

////    @Test
//    @Disabled
//    public void ProductRepository_SaveAll_ReturnSavedProduct() {
//        // given
//        Product product = new Product("whtaburger", 5.99, "This is whataburger");
//        // when
//        Product savedProduct = productRepository.save(product);
//        // then
//        Assertions.assertThat(savedProduct).isNotNull();
//        Assertions.assertThat(savedProduct).isEqualTo(product);
//    }
//
////    @Test
//    @Disabled
//    public void ProductRepository_GetAll_ReturnMoreThanOneProduct() {
//        Product product1 = new Product("whataburger", 5.99, "Delicious whataburger");
//        Product product2 = new Product("double-meat whataburger", 8.99, "Delicious dm_whataburger");
//
//        productRepository.save(product1);
//        productRepository.save(product2);
//
//        List<Product> allProduct = productRepository.findAll();
//
//        Assertions.assertThat(allProduct).isNotNull();
//        Assertions.assertThat(allProduct.size()).isEqualTo(2);
//    }
//
////    @Test
//    @Disabled
//    public void ProductRepository_FindById_ReturnProduct() {
//        Product product = new Product("whataburger", 5.99, "Delicious whataburger");
//
//        productRepository.save(product);
//
//        Product foundProduct = productRepository.findById(product.getId()).get();
//
//        Assertions.assertThat(foundProduct).isNotNull();
//        Assertions.assertThat(foundProduct).isSameAs(product);
//    }
//
////    @Test
//    @Disabled
//    public void Product_Repository_DeleteById_Success() {
//        Product product = new Product("whataburger", 5.99, "Delicious whataburger");
//
//        productRepository.save(product);
//        productRepository.deleteById(product.getId());
//        Optional<Product> foundProduct = productRepository.findById(product.getId());
//
//        Assertions.assertThat(foundProduct).isEmpty();
//    }
}
