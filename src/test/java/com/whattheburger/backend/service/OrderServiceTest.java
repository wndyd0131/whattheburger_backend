package com.whattheburger.backend.service;

import com.whattheburger.backend.controller.dto.OrderCreateRequestDto;
import com.whattheburger.backend.domain.*;
import com.whattheburger.backend.domain.enums.OrderType;
import com.whattheburger.backend.domain.enums.PaymentMethod;
import com.whattheburger.backend.repository.*;
import com.whattheburger.backend.service.exception.ProductNotFoundException;
import com.whattheburger.backend.utils.MockEntityFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    OrderRepository orderRepository;
    @Mock
    ProductRepository productRepository;
    @Mock
    ProductOptionRepository productOptionRepository;
    @Mock
    ProductOptionTraitRepository productOptionTraitRepository;
    @Mock
    OrderProductRepository orderProductRepository;
    @Mock
    OrderProductOptionRepository orderProductOptionRepository;
    @Mock
    OrderProductOptionTraitRepository orderProductOptionTraitRepository;

    @InjectMocks
    OrderService orderService;

    Category mockCategory;
    Option mockOption;
    OptionTrait mockOptionTrait;
    Product mockProduct;
    CustomRule mockCustomRule;
    CategoryProduct mockCategoryProduct;
    ProductOption mockProductOption;
    ProductOptionTrait mockProductOptionTrait;

    @BeforeEach
    void setUp() {
        mockCategory = MockEntityFactory.createMockCategory();
        mockOption = MockEntityFactory.createMockOption();
        mockOptionTrait = MockEntityFactory.createMockOptionTrait();
        mockProduct = MockEntityFactory.createMockProduct();
        mockCustomRule = MockEntityFactory.createMockCustomRule();
        mockCategoryProduct = MockEntityFactory.createMockCategoryProduct(mockCategory, mockProduct);
        mockProductOption = MockEntityFactory.createMockProductOption(mockProduct, mockOption, mockCustomRule);
        mockProductOptionTrait = MockEntityFactory.createMockProductOptionTrait(mockProductOption, mockOptionTrait);
    }

    @Test
    void givenValidRequest_whenCreateOrder_thenOrderIsSavedAndReturned() {
        Order mockOrder = Order
                .builder()
                .id(1L)
                .orderType(OrderType.DELIVERY)
                .orderNote("order note")
                .paymentMethod(PaymentMethod.CASH)
                .totalPrice(0D)
                .discountPrice(0D)
                .couponApplied(true)
                .build();
        OrderProduct mockOrderProduct = OrderProduct
                .builder()
                .id(1L)
                .quantity(1)
                .order(mockOrder)
                .product(mockProduct)
                .forWhom("")
                .build();
        OrderProductOption mockOrderProductOption = OrderProductOption
                .builder()
                .id(1L)
                .quantity(1)
                .build();
        OrderProductOptionTrait mockOrderProductOptionTrait = OrderProductOptionTrait
                .builder()
                .id(1L)
                .value(0)
                .build();
        List<Product> mockProducts = List.of(mockProduct);
        List<ProductOption> mockProductOptions = List.of(mockProductOption);
        List<ProductOptionTrait> mockProductOptionTraits = List.of(mockProductOptionTrait);

        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);
        when(productRepository.findAllById(any(Iterable.class))).thenReturn(mockProducts);
        when(productOptionRepository.findAllById(any(Iterable.class))).thenReturn(mockProductOptions);
        when(productOptionTraitRepository.findAllById(any(Iterable.class))).thenReturn(mockProductOptionTraits);
        when(orderProductRepository.save(any(OrderProduct.class))).thenReturn(mockOrderProduct);
        when(orderProductOptionRepository.save(any(OrderProductOption.class))).thenReturn(mockOrderProductOption);
        when(orderProductOptionTraitRepository.save(any(OrderProductOptionTrait.class))).thenReturn(mockOrderProductOptionTrait);

        OrderCreateRequestDto orderCreateRequestDto = createOrderCreateRequest();
        Order order = orderService.createOrder(orderCreateRequestDto);

        Assertions.assertNotNull(order);
    }

    @Test
    void givenNonExistingProduct_whenCreateOrder_thenThrowNotFoundException() {
        List<Product> mockProducts = List.of(
                Product
                        .builder()
                        .id(2L)
                        .build()
        );

        when(productRepository.findAllById(any(Iterable.class))).thenReturn(mockProducts);

        OrderCreateRequestDto orderCreateRequestDto = createOrderCreateRequest();

        Assertions.assertThrows(ProductNotFoundException.class, () ->
                orderService.createOrder(orderCreateRequestDto)
        );

        verify(productRepository).findAllById(any(Iterable.class));
    }

    private OrderCreateRequestDto createOrderCreateRequest() {
        return OrderCreateRequestDto
                .builder()
                .orderType(OrderType.DELIVERY)
                .orderNote("order note")
                .paymentMethod(PaymentMethod.CASH)
                .totalPrice(0D)
                .couponApplied(true)
                .discountPrice(0D)
                .productRequests(
                        List.of(
                                OrderCreateRequestDto.ProductRequest
                                        .builder()
                                        .productId(1L)
                                        .quantity(1)
                                        .forWhom("")
                                        .productOptionRequests(
                                                List.of(
                                                        OrderCreateRequestDto.ProductOptionRequest
                                                        .builder()
                                                        .productOptionId(1L)
                                                        .quantity(1)
                                                        .productOptionTraitRequests(
                                                                List.of(OrderCreateRequestDto.ProductOptionTraitRequest
                                                                        .builder()
                                                                        .productOptionTraitId(1L)
                                                                        .value(0)
                                                                        .build()
                                                                )
                                                        )
                                                        .build()
                                                )
                                        )
                                        .build()
                        )

                )
                .build();
    }
}
