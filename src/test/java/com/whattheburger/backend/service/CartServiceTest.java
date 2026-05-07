package com.whattheburger.backend.service;

import com.whattheburger.backend.controller.dto.cart.*;
import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.domain.cart.*;
import com.whattheburger.backend.domain.enums.*;
import com.whattheburger.backend.dto_mapper.CartDtoMapper;
import com.whattheburger.backend.repository.*;
import com.whattheburger.backend.service.exception.cart.StoreProductNotInStoreException;
import com.whattheburger.backend.security.UserDetailsImpl;
import com.whattheburger.backend.service.dto.cart.*;
import com.whattheburger.backend.utils.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {


    @Mock ProductRepository productRepository;
    @Mock OptionRepository optionRepository;
    @Mock ProductOptionRepository productOptionRepository;
    @Mock ProductOptionOptionQuantityRepository productOptionOptionQuantityRepository;
    @Mock CustomRuleRepository customRuleRepository;
    @Mock OptionTraitRepository optionTraitRepository;
    @Mock ProductOptionTraitRepository productOptionTraitRepository;
    @Mock StoreRepository storeRepository;
    @Mock StoreProductRepository storeProductRepository;
    @Mock
    CartSessionStorage cartSessionStorage;
    @Mock CartCalculator cartCalculator;

    @Mock
    RedisTemplate<String, CartList> rt;
    @Mock
    Authentication authentication;
    @Mock
    CartDtoMapper cartDtoMapper;
    @Mock
    ValueOperations<String, CartList> valueOperations;
    @Spy
    @InjectMocks
    CartService cartService;

    User mockUser;
    Option mockOption;
    OptionTrait mockOptionTrait;
    Product mockProduct;
    CustomRule mockCustomRule;
    ProductOption mockProductOption;
    ProductOptionTrait mockProductOptionTrait;
    CartList mockCartList;

    @Captor
    ArgumentCaptor<String> keyCaptor;

    @BeforeEach
    public void setUp() {
        initMock();
        ReflectionTestUtils.setField(cartService, "cartValidator", new CartValidator()); // real instance of CartValidator
    }

    @Test
    public void givenTwoCarts_mergeCart_mergesCorrectly() throws Exception {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                new UserDetailsImpl(mockUser),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        Long storeId = 1L;
        UUID guestId = UUID.randomUUID();
        Cart cartItem1 = new Cart(
                1L, 1, Collections.emptyList()
        );
        Cart cartItem2 = new Cart(
                2L, 1, Collections.emptyList()
        );

        CartList userCartList = new CartList(storeId, new ArrayList<>(
                List.of(cartItem1)
        ));
        CartList guestCartList = new CartList(storeId, new ArrayList<>(
                List.of(cartItem2)
        ));

        when(storeRepository.findById(anyLong())).thenReturn(Optional.of(new Store()));
        when(cartSessionStorage.load(anyString())).thenReturn(Optional.of(userCartList), Optional.of(guestCartList));
        doReturn(new ProcessedCartDto()).when(cartService).loadCart(anyLong(), any(), any());

        cartService.mergeCart(storeId, guestId, authentication);

        verify(cartSessionStorage, times(2)).save(anyString(), any(CartList.class));
//
        Assertions.assertThat(userCartList.getCarts().size()).isEqualTo(2);
    }

    @Test
    public void givenTwoEmptyCarts_mergeCart_returnsEmptyList() throws Exception {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                new UserDetailsImpl(mockUser),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        Long storeId = 1L;
        UUID guestId = UUID.randomUUID();
        CartList userCartList = new CartList(storeId, new ArrayList<>());
        CartList guestCartList = new CartList(storeId, new ArrayList<>());

        when(storeRepository.findById(anyLong())).thenReturn(Optional.of(new Store()));
        when(cartSessionStorage.load(anyString())).thenReturn(Optional.of(userCartList), Optional.of(guestCartList));
        doReturn(new ProcessedCartDto()).when(cartService).loadCart(anyLong(), any(), any());

        cartService.mergeCart(storeId, guestId, authentication);

        verify(cartSessionStorage, times(2)).save(anyString(), any(CartList.class));
//
        Assertions.assertThat(userCartList.getCarts().size()).isEqualTo(0);
    }

    @Test
    public void givenUnauthenticatedAuthentication_mergeCart_throwsAuthenticationException() throws Exception {
        authentication.setAuthenticated(false);

        Long storeId = 1L;
        UUID guestId = UUID.randomUUID();

        when(storeRepository.findById(anyLong())).thenReturn(Optional.of(new Store()));

        assertThrows(IllegalStateException.class, () -> cartService.mergeCart(storeId, guestId, authentication));
    }

    @Test
    public void givenCartCreateRequest_whenItemDoesNotBelongToStore_thenThrowException() throws Exception {
        Long storeId = 1L;
        UUID guestId = null;
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                new UserDetailsImpl(mockUser),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        Long foreignStoreId = 99L;
        Long storeProductId = 42L;
        CartCreateRequestDto request = CartCreateRequestDto.builder()
                .storeProductId(storeProductId)
                .quantity(1)
                .customRuleRequests(Collections.emptyList())
                .build();

        StoreProduct storeProductForOtherStore = StoreProduct.builder()
                .id(storeProductId)
                .store(Store.builder().id(foreignStoreId).build())
                .product(mockProduct)
                .build();

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(Store.builder().id(storeId).build()));
        when(cartSessionStorage.load(anyString())).thenReturn(Optional.of(new CartList(storeId, new ArrayList<>())));
        when(storeProductRepository.findAllById(any())).thenReturn(List.of(storeProductForOtherStore));
        when(customRuleRepository.findAllById(any())).thenReturn(Collections.emptyList());
        when(productOptionRepository.findAllById(any())).thenReturn(Collections.emptyList());
        when(productOptionTraitRepository.findAllById(any())).thenReturn(Collections.emptyList());
        when(productOptionOptionQuantityRepository.findAllById(any())).thenReturn(Collections.emptyList());

        assertThrows(StoreProductNotInStoreException.class,
                () -> cartService.saveCart(storeId, guestId, authentication, request));

        verify(storeRepository).findById(storeId);
        verify(storeProductRepository).findAllById(any());
        verify(cartSessionStorage, never()).save(anyString(), any(CartList.class));
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
