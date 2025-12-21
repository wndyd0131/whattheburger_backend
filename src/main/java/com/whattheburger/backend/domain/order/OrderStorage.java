package com.whattheburger.backend.domain.order;

import com.whattheburger.backend.domain.enums.OrderStatus;
import com.whattheburger.backend.domain.enums.PaymentStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderStorage {
    Order save(Order order);
    List<Order> loadByUserInfoAndPaymentStatuses(OrderOwnerKey orderOwnerKey, Collection<PaymentStatus> paymentStatuses);
    Optional<Order> loadByOrderNumber(UUID orderNumber);
    Optional<Order> loadByOrderNumber(UUID orderNumber, Long userId);
    Optional<Order> loadPreviewByUserInfoAndOrderNumberAndOrderStatuses(OrderPreviewOwnerKey orderPreviewOwnerKey, UUID orderNumber, Collection<OrderStatus> orderStatuses);
    Optional<Order> loadByUserInfoAndOrderNumberAndPaymentStatuses(OrderOwnerKey orderOwnerKey, UUID orderNumber, Collection<PaymentStatus> paymentStatuses);
    Optional<Order> loadByCheckoutSessionId(String checkoutSessionId);
//    Optional<Order> loadByEmailAndOrderNumberAndPaymentStatuses(String email, UUID orderNumber, Collection<PaymentStatus> paymentStatuses);
}
