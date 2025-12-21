package com.whattheburger.backend.controller;

import com.whattheburger.backend.controller.dto.order.*;
import com.whattheburger.backend.controller.dto_mapper.OrderResponseDtoMapper;
import com.whattheburger.backend.controller.dto_mapper.OrderSessionResponseDtoMapper;
import com.whattheburger.backend.domain.enums.OrderType;
import com.whattheburger.backend.domain.order.Order;
import com.whattheburger.backend.domain.order.OrderSession;
import com.whattheburger.backend.service.CheckoutService;
import com.whattheburger.backend.service.OrderService;
import com.whattheburger.backend.service.StoreService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order")
public class OrderController {

    private final OrderService orderService;
    private final CheckoutService checkoutService;
    private final OrderResponseDtoMapper orderResponseDtoMapper;
    private final OrderSessionResponseDtoMapper orderSessionResponseDtoMapper;
    private final StoreService storeService;

    //    @PostMapping()
//    public ResponseEntity<String> createOrder(@RequestBody OrderCreateRequestDto orderCreateRequestDto) {
//        orderService.createOrder(orderCreateRequestDto);
//        return new ResponseEntity<String>(
//                "Order successfully created",
//                HttpStatus.CREATED
//        );
//    }
    @PostMapping("/api/v1/order-session/store/{storeId}")
    public ResponseEntity<OrderSessionResponseDto> createOrderSession(
            @RequestBody OrderSessionCreateRequestDto orderSessionRequestDto,
            @PathVariable(name = "storeId") Long storeId,
            @CookieValue(name = "guestId") UUID guestId,
            @CookieValue(name = "orderSessionId", required = false)  UUID orderSessionId,
            Authentication authentication,
            HttpServletResponse response
    ) {
        OrderType orderType = orderSessionRequestDto.getOrderType();
        OrderSession orderSession = orderService.createOrderSession(storeId, guestId, orderSessionId, authentication, orderType);
        OrderSessionResponseDto orderSessionResponseDto = orderSessionResponseDtoMapper.toOrderSessionResponseDto(orderSession);
        ResponseCookie cookie = ResponseCookie.from("orderSessionId", orderSession.getSessionId().toString())
                .path("/")
                .secure(true)
                .httpOnly(true)
                .sameSite("None")
                .maxAge(Duration.ofDays(30))
                .build();
        response.setHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString()
        );
        return new ResponseEntity<>(
                orderSessionResponseDto,
                HttpStatus.CREATED
        );
    }

    @GetMapping("/api/v1/order-session/{sessionId}/store/{storeId}")
    public ResponseEntity<OrderSessionResponseDto> loadOrderSession(
            @CookieValue(name = "guestId") UUID guestId,
            @PathVariable(name = "sessionId") UUID sessionId,
            @PathVariable(name = "storeId") Long storeId,
            Authentication authentication
    ) {
        OrderSession orderSession = orderService.loadOrderSession(storeId, sessionId, guestId, authentication);
        OrderSessionResponseDto orderSessionResponseDto = orderSessionResponseDtoMapper.toOrderSessionResponseDto(orderSession);
        return ResponseEntity.ok(orderSessionResponseDto);
    }

//    @GetMapping("/api/v1/order/{orderNumber}")
//    public ResponseEntity<OrderResponseDto> loadPaidOrderByOrderNumberAndVerification(
//            @PathVariable("orderNumber") UUID orderNumber,
//            @RequestBody GuestOrderLookUpRequestDto requestDto,
//            Authentication authentication
//    ) {
//        String guestEmail = requestDto.getEmail();
//        Order order = orderService.loadOrder(orderNumber, authentication, guestEmail);
//        return ResponseEntity.ok(
//                orderResponseDtoMapper.toOrderDetailResponseDto(order)
//        );
//    }

    @GetMapping("/api/v1/order-session/checkout-session/{sessionId}")
    public ResponseEntity<OrderSessionResponseDto> loadOrderSessionByCheckoutSessionId(
            @PathVariable(name = "sessionId") String checkoutSessionId
    ) {
        String orderSessionId = checkoutService.getOrderSessionId(checkoutSessionId);
        OrderSession orderSession = orderService.loadOrderSessionByOrderSessionId(UUID.fromString(orderSessionId));
        OrderSessionResponseDto orderSessionResponseDto = orderSessionResponseDtoMapper.toOrderSessionResponseDto(orderSession);
        return ResponseEntity.ok(orderSessionResponseDto);
    }

    @GetMapping("/api/v1/order/list")
    public ResponseEntity<List<OrderResponseDto>> fetchAllOrders(
            Authentication authentication
    ) {
        List<Order> orders = orderService.loadOrders(authentication);
        List<OrderResponseDto> orderResponseDtos = orderResponseDtoMapper.toOrderResponseDto(orders);
        return ResponseEntity.ok(
                orderResponseDtos
        );
    }

    @GetMapping("/api/v1/order/{orderNumber}/detail")
    public ResponseEntity<OrderDetailResponseDto> fetchOrderDetail(
            @PathVariable(name = "orderNumber") UUID orderNumber,
            Authentication authentication
    ) {
        Order order = orderService.loadOrder(orderNumber, authentication);
        OrderDetailResponseDto orderDetailResponseDto = orderResponseDtoMapper.toOrderDetailResponseDto(order);
        return ResponseEntity.ok(
                orderDetailResponseDto
        );
    }

    @GetMapping("/api/v1/order/checkout-session/{sessionId}")
    public ResponseEntity<OrderDetailResponseDto> loadOrderByCheckoutSessionId(
            @PathVariable(name = "sessionId") String checkoutSessionId,
            @CookieValue(name = "guestId") UUID guestId,
            Authentication authentication
    ) {
        log.info("Checkout called {}", checkoutSessionId);
        Order order = orderService.loadOrderByCheckoutSessionId(guestId, authentication, checkoutSessionId);
        return ResponseEntity.ok(
                orderResponseDtoMapper.toOrderDetailResponseDto(order)
        );
    }
}
