package com.whattheburger.backend.repository;

import com.whattheburger.backend.domain.StoreProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface StoreProductRepository extends JpaRepository<StoreProduct, Long> {
    Optional<StoreProduct> findByStoreIdAndProductId(Long storeId, Long productId);
    List<StoreProduct> findByProductIdAndStoreIdIn(Long productId, Collection<Long> storeIds);
    List<StoreProduct> findByStoreIdAndProductIdIn(Long storeId, Collection<Long> productIds);
    List<StoreProduct> findByStoreId(Long storeId);
}
