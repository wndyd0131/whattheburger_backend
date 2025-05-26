package com.whattheburger.backend.repository;

import com.whattheburger.backend.domain.CategoryProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryProductRepository extends JpaRepository<CategoryProduct, Long> {
    List<CategoryProduct> findByCategoryId(Long categoryId);
}
