package com.whataburger.whataburgerproject.repository;

import com.whataburger.whataburgerproject.domain.CategoryProduct;
import com.whataburger.whataburgerproject.domain.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryProductRepository extends JpaRepository<CategoryProduct, Long> {
    List<CategoryProduct> findByCategoryId(Long categoryId);
}
