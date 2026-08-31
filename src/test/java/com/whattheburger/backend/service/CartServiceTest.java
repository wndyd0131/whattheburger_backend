package com.whattheburger.backend.service;

import com.whattheburger.backend.controller.dto.cart.*;
import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.domain.cart.*;
import com.whattheburger.backend.dto_mapper.CartDtoMapper;
import com.whattheburger.backend.repository.*;
import com.whattheburger.backend.service.exception.cart.InsufficientOptionStockException;
import com.whattheburger.backend.service.exception.cart.CartOwnerRequiredException;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

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
    @Mock StoreInventoryRepository storeInventoryRepository;
    @Mock
    CartSessionStorage cartSessionStorage;
    @Mock CartValidator cartValidator;
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
        when(cartValidator.canMergeItemCount(anyInt(), anyInt())).thenReturn(true);
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
        when(cartValidator.canMergeItemCount(anyInt(), anyInt())).thenReturn(true);
        when(cartSessionStorage.load(anyString())).thenReturn(Optional.of(userCartList), Optional.of(guestCartList));
        doReturn(new ProcessedCartDto()).when(cartService).loadCart(anyLong(), any(), any());

        cartService.mergeCart(storeId, guestId, authentication);

        verify(cartSessionStorage, times(2)).save(anyString(), any(CartList.class));
//
        Assertions.assertThat(userCartList.getCarts().size()).isEqualTo(0);
    }

    @Test
    public void givenMergeWouldExceedLimit_mergeCart_doesNotSaveAndReturnsLoadCart() throws Exception {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                new UserDetailsImpl(mockUser),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        Long storeId = 1L;
        UUID guestId = UUID.randomUUID();
        List<Cart> userCarts = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            userCarts.add(new Cart((long) i, 1, Collections.emptyList()));
        }
        List<Cart> guestCarts = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            guestCarts.add(new Cart(100L + i, 1, Collections.emptyList()));
        }
        CartList userCartList = new CartList(storeId, userCarts);
        CartList guestCartList = new CartList(storeId, guestCarts);

        when(storeRepository.findById(anyLong())).thenReturn(Optional.of(new Store()));
        when(cartValidator.canMergeItemCount(15, 6)).thenReturn(false);
        when(cartSessionStorage.load(anyString())).thenReturn(Optional.of(userCartList), Optional.of(guestCartList));
        doReturn(new ProcessedCartDto()).when(cartService).loadCart(anyLong(), any(), any());

        cartService.mergeCart(storeId, guestId, authentication);

        verify(cartSessionStorage, never()).save(anyString(), any(CartList.class));
        verify(cartService).loadCart(storeId, guestId, authentication);
        Assertions.assertThat(userCartList.getCarts()).hasSize(15);
        Assertions.assertThat(guestCartList.getCarts()).hasSize(6);
    }

    @Test
    public void givenNoGuestAndNoAuth_whenLoadCart_thenThrowsCartOwnerRequiredException() {
        Authentication unauthenticated = mock(Authentication.class);
        when(unauthenticated.isAuthenticated()).thenReturn(false);

        assertThrows(CartOwnerRequiredException.class,
                () -> cartService.loadCart(1L, null, unauthenticated));

        verifyNoInteractions(cartSessionStorage);
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
    public void givenValidatorThrows_whenSaveCart_thenPropagatesAndDoesNotSaveSession() {
        Long storeId = 1L;
        Long storeProductId = 42L;
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                new UserDetailsImpl(mockUser),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        CartCreateRequestDto request = CartCreateRequestDto.builder()
                .storeProductId(storeProductId)
                .quantity(1)
                .customRuleRequests(Collections.emptyList())
                .build();

        when(storeRepository.findById(storeId)).thenReturn(Optional.of(Store.builder().id(storeId).build()));
        when(cartSessionStorage.load(anyString())).thenReturn(Optional.of(new CartList(storeId, new ArrayList<>())));
        doThrow(new InsufficientOptionStockException(1L, 2L, 5, 0))
                .when(cartValidator).validate(any(CartList.class), any(), any(), any(), any(), any(), any());

        assertThrows(InsufficientOptionStockException.class,
                () -> cartService.saveCart(storeId, /*guestId*/ null, authentication, request));

        verify(storeInventoryRepository).findAllByStoreId(storeId);
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
