package com.whattheburger.backend.controller;

import com.whattheburger.backend.controller.dto.order.OrderDetailResponseDto;
import com.whattheburger.backend.controller.dto_mapper.OrderResponseDtoMapper;
import com.whattheburger.backend.controller.dto_mapper.OrderSessionResponseDtoMapper;
import com.whattheburger.backend.domain.enums.OrderStatus;
import com.whattheburger.backend.domain.enums.OrderType;
import com.whattheburger.backend.domain.enums.PaymentMethod;
import com.whattheburger.backend.domain.enums.PaymentStatus;
import com.whattheburger.backend.domain.order.Order;
import com.whattheburger.backend.security.JwtFilter;
import com.whattheburger.backend.service.CheckoutService;
import com.whattheburger.backend.service.OrderService;
import com.whattheburger.backend.service.StoreService;
import com.whattheburger.backend.service.exception.OrderNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private OrderResponseDtoMapper orderResponseDtoMapper;

    @MockBean
    private CheckoutService checkoutService;

    @MockBean
    private OrderSessionResponseDtoMapper orderSessionResponseDtoMapper;

    @MockBean
    private StoreService storeService;

    @MockBean
    private JwtFilter jwtFilter;

    private static final UUID ORDER_NUMBER = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    @Test
    @WithMockUser
    void fetchOrderDetail_whenOrderExistsAndAuthenticated_returns200WithCorrectJson() throws Exception {
        Order order = Order.builder()
                .id(1L)
                .orderNumber(ORDER_NUMBER)
                .totalPrice(BigDecimal.valueOf(19.99))
                .orderStatus(OrderStatus.PENDING)
                .orderType(OrderType.DELIVERY)
                .paymentStatus(PaymentStatus.PAID)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .orderProducts(Collections.emptyList())
                .build();

        OrderDetailResponseDto expectedDto = OrderDetailResponseDto.builder()
                .id(1L)
                .orderNumber(ORDER_NUMBER)
                .totalPrice(BigDecimal.valueOf(19.99))
                .orderStatus(OrderStatus.PENDING)
                .orderType(OrderType.DELIVERY)
                .paymentStatus(PaymentStatus.PAID)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .orderDate(order.getCreatedAt())
                .productResponse(Collections.emptyList())
                .build();

        when(orderService.loadOrder(eq(ORDER_NUMBER), any())).thenReturn(order);
        when(orderResponseDtoMapper.toOrderDetailResponseDto(order)).thenReturn(expectedDto);

        mockMvc.perform(get("/api/v1/order/{orderNumber}/detail", ORDER_NUMBER)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderNumber").value(ORDER_NUMBER.toString()))
                .andExpect(jsonPath("$.totalPrice").value(19.99))
                .andExpect(jsonPath("$.orderStatus").value("PENDING"))
                .andExpect(jsonPath("$.orderType").value("DELIVERY"))
                .andExpect(jsonPath("$.paymentStatus").value("PAID"))
                .andExpect(jsonPath("$.paymentMethod").value("CARD"));

        verify(orderService).loadOrder(eq(ORDER_NUMBER), any());
        verify(orderResponseDtoMapper).toOrderDetailResponseDto(order);
    }

    @Test
    void fetchOrderDetail_whenNotAuthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/order/{orderNumber}/detail", ORDER_NUMBER)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void fetchOrderDetail_whenOrderNotFound_returns404() throws Exception {
        when(orderService.loadOrder(eq(ORDER_NUMBER), any()))
                .thenThrow(OrderNotFoundException.forOrder(ORDER_NUMBER));

        mockMvc.perform(get("/api/v1/order/{orderNumber}/detail", ORDER_NUMBER)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Available order with order ID[" + ORDER_NUMBER + "] not found"))
                .andExpect(jsonPath("$.status").value(404));

        verify(orderService).loadOrder(eq(ORDER_NUMBER), any());
    }
}
