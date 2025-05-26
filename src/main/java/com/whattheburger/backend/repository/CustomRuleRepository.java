package com.whattheburger.backend.repository;

import com.whattheburger.backend.domain.CustomRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomRuleRepository extends JpaRepository<CustomRule, Long> {
}
