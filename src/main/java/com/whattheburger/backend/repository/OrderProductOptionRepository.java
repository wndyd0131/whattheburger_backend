package com.whattheburger.backend.repository;

import com.whattheburger.backend.domain.order.OrderProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderProductOptionRepository extends JpaRepository<OrderProductOption, Long> {
}

