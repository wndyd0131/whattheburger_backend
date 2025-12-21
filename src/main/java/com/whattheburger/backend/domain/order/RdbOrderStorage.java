package com.whattheburger.backend.domain.order;

import com.whattheburger.backend.domain.enums.OrderStatus;
import com.whattheburger.backend.domain.enums.PaymentStatus;
import com.whattheburger.backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RdbOrderStorage implements OrderStorage {
    private final OrderRepository orderRepository;
    @Override
    public Order save(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public Optional<Order> loadByOrderNumber(UUID orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    @Override
    public Optional<Order> loadByOrderNumber(UUID orderNumber, Long userId) {
        return orderRepository.findByUserIdAndOrderNumber(userId, orderNumber);
    }

    @Override
    public Optional<Order> loadByUserInfoAndOrderNumberAndPaymentStatuses(OrderOwnerKey orderOwnerKey, UUID orderNumber, Collection<PaymentStatus> paymentStatuses) {
        if (orderOwnerKey instanceof UserKey u) {
            return orderRepository.findByUserIdAndOrderNumberAndPaymentStatusIn(u.userId(), orderNumber, paymentStatuses);
        } else if (orderOwnerKey instanceof GuestKey g) {
            return orderRepository.findByContactInfo_EmailAndOrderNumberAndPaymentStatusIn(g.email(), orderNumber, paymentStatuses);
        } else {
            throw new IllegalStateException(); // not valid key exception
        }
    }

    @Override
    public Optional<Order> loadByCheckoutSessionId(String checkoutSessionId) {
        return orderRepository.findByCheckoutSessionId(checkoutSessionId);
    }

    @Override
    public Optional<Order> loadPreviewByUserInfoAndOrderNumberAndOrderStatuses(OrderPreviewOwnerKey orderPreviewOwnerKey, UUID orderNumber, Collection<OrderStatus> orderStatuses) {
        if (orderPreviewOwnerKey instanceof UserPreviewKey u) {
            return orderRepository.findByUserIdAndOrderNumberAndOrderStatusIn(u.userId(), orderNumber, orderStatuses);
        } else if (orderPreviewOwnerKey instanceof GuestPreviewKey g) {
            return orderRepository.findByGuestInfo_GuestIdAndOrderNumberAndOrderStatusIn(g.guestId(), orderNumber, orderStatuses);
        } else {
            throw new IllegalStateException(); // not valid key exception
        }
    }

//    @Override
//    public Optional<Order> loadByEmailAndOrderNumberAndPaymentStatuses(String email, UUID orderNumber, Collection<PaymentStatus> paymentStatuses) {
//        return orderRepository.findByContactInfo_EmailAndOrderNumberAndPaymentStatusIn(email, orderNumber, paymentStatuses);
//    }

    @Override
    public List<Order> loadByUserInfoAndPaymentStatuses(OrderOwnerKey orderOwnerKey, Collection<PaymentStatus> paymentStatuses) {
        if (orderOwnerKey instanceof UserKey u) {
            return orderRepository.findByUserIdAndPaymentStatusIn(u.userId(), paymentStatuses);
        } else {
            throw new IllegalStateException(); // not valid key exception
        }
    }



}
