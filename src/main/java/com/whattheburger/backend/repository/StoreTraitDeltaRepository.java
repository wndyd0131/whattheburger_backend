package com.whattheburger.backend.repository;

import com.whattheburger.backend.domain.StoreTraitDelta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreTraitDeltaRepository extends JpaRepository<StoreTraitDelta, Long> {
}
