package com.whataburger.whataburgerproject.repository;

import com.whataburger.whataburgerproject.domain.Option;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OptionRepository extends JpaRepository<Option, Long> {
}
