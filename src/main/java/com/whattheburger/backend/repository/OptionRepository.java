package com.whattheburger.backend.repository;

import com.whattheburger.backend.domain.Option;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OptionRepository extends JpaRepository<Option, Long> {
}
