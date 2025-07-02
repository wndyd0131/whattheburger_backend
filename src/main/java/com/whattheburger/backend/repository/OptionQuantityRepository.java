package com.whattheburger.backend.repository;

import com.whattheburger.backend.domain.OptionQuantity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OptionQuantityRepository extends JpaRepository<OptionQuantity, Long> {
}
