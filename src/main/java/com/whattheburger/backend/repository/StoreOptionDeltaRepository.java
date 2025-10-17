package com.whattheburger.backend.repository;

import com.whattheburger.backend.domain.StoreOptionDelta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreOptionDeltaRepository extends JpaRepository<StoreOptionDelta, Long> {
    List<StoreOptionDelta> findByStoreProductId(Long storeProductId);
}
