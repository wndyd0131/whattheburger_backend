package com.whattheburger.backend.repository;

import com.whattheburger.backend.domain.ProductOptionOptionQuantity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ProductOptionOptionQuantityRepository extends JpaRepository<ProductOptionOptionQuantity, Long> {

    @Query("""
            SELECT DISTINCT pooq FROM ProductOptionOptionQuantity pooq
            JOIN FETCH pooq.optionQuantity oq
            JOIN FETCH oq.optionQuantityIngredients
            WHERE pooq.id IN :ids
            """)
    List<ProductOptionOptionQuantity> findAllWithOptionQuantityIngredientsByIdIn(@Param("ids") Collection<Long> ids);
}
