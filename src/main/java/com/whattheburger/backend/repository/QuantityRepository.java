package com.whattheburger.backend.repository;

import com.whattheburger.backend.domain.Quantity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuantityRepository extends JpaRepository<Quantity, Long> {
}
