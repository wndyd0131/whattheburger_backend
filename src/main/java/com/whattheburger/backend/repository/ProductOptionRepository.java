package com.whattheburger.backend.repository;

import com.whattheburger.backend.domain.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {

    List<ProductOption> findByProductId(Long productId);

    Optional<ProductOption> findByProductIdAndOptionId(Long productId, Long optionId);

    @Query("""
            SELECT DISTINCT po FROM ProductOption po
            JOIN FETCH po.option o
            JOIN FETCH o.optionIngredients
            WHERE po.id IN :ids
            """)
    List<ProductOption> findAllWithOptionIngredientsByIdIn(@Param("ids") Collection<Long> ids);
}
