package com.whattheburger.backend.repository;

import com.whattheburger.backend.domain.ProductOptionTrait;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductOptionTraitRepository extends JpaRepository<ProductOptionTrait, Long> {
    Optional<ProductOptionTrait> findByProductOptionIdAndOptionTraitId(Long productOptionId, Long optionTraitId);
}
