package com.whattheburger.backend.integration.support;

import com.whattheburger.backend.domain.Address;
import com.whattheburger.backend.domain.Coordinate;
import com.whattheburger.backend.domain.Product;
import com.whattheburger.backend.domain.Store;
import com.whattheburger.backend.domain.StoreProduct;
import com.whattheburger.backend.domain.enums.ProductType;
import com.whattheburger.backend.repository.ProductRepository;
import com.whattheburger.backend.repository.StoreProductRepository;
import com.whattheburger.backend.repository.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CatalogIntegrationFixture {

    @Autowired
    StoreRepository storeRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    StoreProductRepository storeProductRepository;

    public Store saveStore(String branch) {
        return storeRepository.save(createStore(branch));
    }

    public StoreProduct saveMinimalStoreProduct(Store store) {
        return saveMinimalStoreProduct(store, "Burger");
    }

    public StoreProduct saveMinimalStoreProduct(Store store, String productName) {
        Product product = productRepository.save(
                new Product(productName, BigDecimal.valueOf(5.99), "brief", 590D, ProductType.ONLY)
        );
        return storeProductRepository.save(new StoreProduct(store, product));
    }

    private Store createStore(String branch) {
        return Store.builder()
                .branch(branch)
                .houseNumber(1L)
                .phoneNum("512-123-4567")
                .website("www.whattheburger.com")
                .address(new Address("Austin", "123 Main St", "TX", "78701"))
                .coordinate(new Coordinate(30.0, -97.0))
                .build();
    }
}
