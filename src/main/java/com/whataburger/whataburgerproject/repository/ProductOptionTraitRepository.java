package com.whataburger.whataburgerproject.repository;

import com.whataburger.whataburgerproject.domain.ProductOptionTrait;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductOptionTraitRepository extends JpaRepository<ProductOptionTrait, Long> {
    Optional<ProductOptionTrait> findByProductOptionIdAndOptionTraitId(Long productOptionId, Long optionTraitId);
}
