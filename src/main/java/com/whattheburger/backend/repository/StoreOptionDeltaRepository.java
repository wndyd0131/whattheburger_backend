package com.whattheburger.backend.repository;

import com.whattheburger.backend.domain.StoreOptionDelta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreOptionDeltaRepository extends JpaRepository<StoreOptionDelta, Long> {
    List<StoreOptionDelta> findByStoreProductId(Long storeProductId);

    Optional<StoreOptionDelta> findByStoreProductIdAndProductOptionId(Long storeProductId, Long productOptionId);
}
