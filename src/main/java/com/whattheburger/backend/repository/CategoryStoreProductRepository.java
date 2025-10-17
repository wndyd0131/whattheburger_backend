package com.whattheburger.backend.repository;

import com.whattheburger.backend.domain.CategoryStoreProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryStoreProductRepository extends JpaRepository<CategoryStoreProduct, Long> {
}
