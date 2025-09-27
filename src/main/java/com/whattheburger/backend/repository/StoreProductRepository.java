package com.whattheburger.backend.repository;

import com.whattheburger.backend.domain.StoreProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreProductRepository extends JpaRepository<StoreProduct, Long> {
}
