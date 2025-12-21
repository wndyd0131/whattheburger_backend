package com.whattheburger.backend.service;

import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.domain.cart.CartList;
import com.whattheburger.backend.domain.order.Order;
import com.whattheburger.backend.repository.OrderRepository;
import com.whattheburger.backend.security.UserDetailsImpl;
import com.whattheburger.backend.utils.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

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
        Assertions.assertDoesNotThrow(() -> new IllegalStateException());
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
