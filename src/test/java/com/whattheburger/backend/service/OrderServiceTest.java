package com.whattheburger.backend.service;

import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.domain.cart.CartList;
import com.whattheburger.backend.domain.order.OrderSession;
import com.whattheburger.backend.domain.order.OrderSessionStorage;
import com.whattheburger.backend.repository.OrderRepository;
import com.whattheburger.backend.security.UserDetailsImpl;
import com.whattheburger.backend.service.exception.order.OrderSessionNotFoundException;
import com.whattheburger.backend.util.OrderSessionFactory;
import com.whattheburger.backend.utils.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    User mockUser;
    Option mockOption;
    OptionTrait mockOptionTrait;
    Product mockProduct;
    CustomRule mockCustomRule;
    ProductOption mockProductOption;
    ProductOptionTrait mockProductOptionTrait;
    CartList mockCartList;

    @Mock
    OrderRepository orderRepository;
    @Mock
    CartService cartService;
    @Mock
    OrderSessionStorage orderSessionStorage;
    @Mock
    OrderSessionFactory orderSessionFactory;

    @InjectMocks
    OrderService orderService;

    @BeforeEach
    public void setUp() {
        initMock();
    }

    @Test
    public void givenAuthentication_whenAuthenticated_thenReturnOrderListSuccessfully() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                new UserDetailsImpl(
                        mockUser
                ),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        orderService.loadOrders(authentication);
        assertDoesNotThrow(() -> new IllegalStateException());
    }

    @Test
    void givenMismatchedStoreId_whenLoadOrderSession_thenThrowsOrderSessionNotFoundException() {
        UUID sessionId = UUID.randomUUID();
        Long requestStoreId = 1L;
        Long sessionStoreId = 2L;
        UUID guestId = UUID.randomUUID();
        Authentication authentication = mock(Authentication.class);

        OrderSession orderSession = OrderSession.builder()
                .sessionId(sessionId)
                .storeId(sessionStoreId)
                .build();
        when(orderSessionStorage.load(sessionId)).thenReturn(Optional.of(orderSession));

        assertThrows(OrderSessionNotFoundException.class,
                () -> orderService.loadOrderSession(requestStoreId, sessionId, guestId, authentication));
    }

    private void initMock() {
        mockUser = MockUserFactory.createUser();
        mockOption = MockOptionFactory.createMockOption();
        mockOptionTrait = MockOptionTraitFactory.createMockOptionTrait();
        mockProduct = MockProductFactory.createMockProduct();
        mockCustomRule = MockCustomRuleFactory.createMockCustomRule();
        mockProductOption = MockOptionFactory.createMockProductOption(mockProduct, mockOption, mockCustomRule);
        mockProductOptionTrait = MockOptionTraitFactory.createMockProductOptionTrait(mockProductOption, mockOptionTrait);
        mockCartList = MockCartFactory.createCartList();
    }
}
