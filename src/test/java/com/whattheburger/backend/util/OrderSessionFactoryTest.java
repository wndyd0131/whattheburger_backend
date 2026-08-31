package com.whattheburger.backend.util;

import com.whattheburger.backend.domain.enums.OrderType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderSessionFactoryTest {

    private final OrderSessionFactory orderSessionFactory = new OrderSessionFactory();

    @Test
    void givenNullCartDto_whenCreateFromCartDto_thenThrowsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> orderSessionFactory.createFromCartDto(null, 1L, OrderType.PICK_UP, 1L)
        );
    }
}
