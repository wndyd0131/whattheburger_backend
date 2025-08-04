package com.whattheburger.backend.domain.order;

import com.whattheburger.backend.domain.enums.OrderStatus;
import com.whattheburger.backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RdbOrderStorage implements OrderStorage {
    private final OrderRepository orderRepository;
    @Override
    public Order save(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public Optional<Order> load(OrderOwnerKey orderOwnerKey) {
        if (orderOwnerKey instanceof UserKey u) {
            return orderRepository.findByUserIdAndOrderStatus(u.userId(), OrderStatus.PENDING);
        } else if (orderOwnerKey instanceof GuestKey g) {
            return orderRepository.findByGuestInfo_GuestIdAndOrderStatus(g.guestId(), OrderStatus.PENDING);
        } else {
            throw new IllegalStateException(); // not valid key exception
        }
    }

}
