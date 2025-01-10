package com.whataburger.whataburgerproject.repository;

import com.whataburger.whataburgerproject.domain.temp.ProductIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductIngredientRepository extends JpaRepository<ProductIngredient, Long> {
}
