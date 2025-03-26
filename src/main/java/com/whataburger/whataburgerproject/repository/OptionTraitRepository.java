package com.whataburger.whataburgerproject.repository;

import com.whataburger.whataburgerproject.domain.OptionTrait;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OptionTraitRepository extends JpaRepository<OptionTrait, Long> {
}
