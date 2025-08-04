package com.whattheburger.backend.repository;

import com.whattheburger.backend.domain.order.OrderProductOptionTrait;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderProductOptionTraitRepository extends JpaRepository<OrderProductOptionTrait, Long> {
}
