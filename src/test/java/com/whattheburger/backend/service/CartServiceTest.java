package com.whattheburger.backend.service;

import com.whattheburger.backend.controller.dto.cart.*;
import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.domain.cart.*;
import com.whattheburger.backend.domain.enums.*;
import com.whattheburger.backend.dto_mapper.CartDtoMapper;
import com.whattheburger.backend.repository.*;
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

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
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
    @Mock CartSessionStorage cartSessionStorage;

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
    public void givenExistingCart_whenLoadCart_thenReturnsExpectedDto() throws Exception {
//        String cartId = "1";
//        CartList mockCartList = MockCartFactory.createCartList();
//
//        ValidatedProduct validatedProduct = ValidatedProduct
//                .builder()
//                .product(mockProduct)
//                .build();
//        List<ValidatedTrait> validatedTraits = List.of(
//                ValidatedTrait
//                        .builder()
//                        .currentValue(1)
//                        .productOptionTrait(mockProductOptionTrait)
//                        .build()
//        );
//
//        List<ValidatedOption> validatedOptions = List.of(
//                ValidatedOption
//                        .builder()
//                        .isSelected(true)
//                        .quantity(1)
//                        .productOption(mockProductOption)
//                        .validatedTraits(validatedTraits)
//                        .validatedQuantity(null)
//                        .build()
//        );
//        List<ValidatedCustomRule> validatedCustomRules = List.of(
//                ValidatedCustomRule
//                        .builder()
//                        .customRule(mockCustomRule)
//                        .validatedOptions(validatedOptions)
//                        .build()
//        );
//
//        List<ValidatedCartDto> validatedCartDtos = List.of(
//                ValidatedCartDto
//                        .builder()
//                        .validatedProduct(validatedProduct)
//                        .validatedCustomRules(validatedCustomRules)
//                        .build()
//        );
//
//        ProcessedCartDto processedCartDto = ProcessedCartDto
//                .builder()
//                .cartDtos(validatedCartDtos)
//                .totalPrice(BigDecimal.ZERO)
//                .build();
//
//
//        ProductResponse productResponse = ProductResponse
//                .builder()
//                .productId(1L)
//                .productName("Whattheburger")
//                .productPrice(new BigDecimal("5.99"))
//                .imageSource("")
//                .productType(ProductType.ONLY)
//                .build();
//
//        List<OptionTraitResponse> optionTraitResponses = List.of(
//                OptionTraitResponse
//                        .builder()
//                        .currentValue(1)
//                        .labelCode("TBS")
//                        .productOptionTraitId(1L)
//                        .optionTraitName("Toast Both Sides")
//                        .optionTraitType(OptionTraitType.BINARY)
//                        .build()
//        );
//
//        List<OptionResponse> optionResponses = List.of(
//                OptionResponse
//                        .builder()
//                        .countType(CountType.COUNTABLE)
//                        .isSelected(true)
//                        .measureType(MeasureType.COUNT)
//                        .optionName("Large Bun")
//                        .optionQuantity(1)
//                        .optionTraitResponses(optionTraitResponses)
//                        .productOptionId(1L)
//                        .orderIndex(0)
//                        .quantityDetailResponse(null)
//                        .build()
//        );
//        List<CustomRuleResponse> customRuleResponses = List.of(
//                CustomRuleResponse
//                        .builder()
//                        .customRuleId(1L)
//                        .customRuleName("Bread")
//                        .optionResponses(optionResponses)
//                        .orderIndex(0)
//                        .build()
//        );
//
//        List<CartResponse> cartResponses = List.of(
//                CartResponse
//                        .builder()
//                        .productResponse(productResponse)
//                        .customRuleResponses(customRuleResponses)
//                        .build()
//        );
//        CartResponseDto cartResponseDto = CartResponseDto
//                .builder()
//                .cartResponses(cartResponses)
//                .totalPrice(BigDecimal.ZERO)
//                .build();
//
//        when(rt.opsForValue()).thenReturn(valueOperations);
//        when(valueOperations.get("cart:"+cartId)).thenReturn(mockCartList);
//        doReturn(processedCartDto).when(cartService).processCart(anyList());
//        when(cartDtoMapper.toCartResponseDto(processedCartDto)).thenReturn(cartResponseDto);
//
//        CartResponseDto resultDto = cartService.loadCart(cartId, authentication);
//
//        Assertions.assertNotNull(resultDto);
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
