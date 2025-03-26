package com.whataburger.whataburgerproject.repository;

import com.whataburger.whataburgerproject.domain.CustomRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomRuleRepository extends JpaRepository<CustomRule, Long> {
}
