package com.whattheburger.backend.repository;

import com.whattheburger.backend.domain.enums.OrderStatus;
import com.whattheburger.backend.domain.enums.PaymentStatus;
import com.whattheburger.backend.domain.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByUserIdAndOrderStatus(Long userId, OrderStatus orderStatus);
    Optional<Order> findByGuestInfo_GuestIdAndOrderStatus(UUID guestId, OrderStatus orderStatus);
    List<Order> findByUserIdAndPaymentStatusIn(Long userId, Collection<PaymentStatus> paymentStatuses);
    Optional<Order> findByOrderNumber(UUID orderNumber);
    Optional<Order> findByUserIdAndOrderNumberAndOrderStatusIn(Long userId, UUID orderNumber, Collection<OrderStatus> orderStatuses);
    Optional<Order> findByGuestInfo_GuestIdAndOrderNumberAndOrderStatusIn(UUID guestId, UUID orderNumber, Collection<OrderStatus> orderStatuses);
    Optional<Order> findByUserIdAndOrderNumberAndPaymentStatusIn(Long userId, UUID orderNumber, Collection<PaymentStatus> paymentStatuses);
    Optional<Order> findByContactInfo_EmailAndOrderNumberAndPaymentStatusIn(String email, UUID orderNumber, Collection<PaymentStatus> paymentStatuses);
    Optional<Order> findByCheckoutSessionId(String checkoutSessionId);
}
