package com.whattheburger.backend.repository;

import com.whattheburger.backend.domain.enums.OrderStatus;
import com.whattheburger.backend.domain.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByUserIdAndOrderStatus(Long userId, OrderStatus orderStatus);
    Optional<Order> findByGuestInfo_GuestIdAndOrderStatus(UUID guestId, OrderStatus orderStatus);
}
