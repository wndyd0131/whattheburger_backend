package com.whattheburger.backend.controller;

import com.whattheburger.backend.controller.dto.order.*;
import com.whattheburger.backend.controller.dto_mapper.OrderResponseDtoMapper;
import com.whattheburger.backend.controller.dto_mapper.OrderSessionResponseDtoMapper;
import com.whattheburger.backend.domain.enums.OrderType;
import com.whattheburger.backend.domain.order.Order;
import com.whattheburger.backend.domain.order.OrderSession;
import com.whattheburger.backend.service.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order")
public class OrderController {

    private final OrderService orderService;
    private final OrderResponseDtoMapper orderResponseDtoMapper;
    private final OrderSessionResponseDtoMapper orderSessionResponseDtoMapper;

//    @PostMapping()
//    public ResponseEntity<String> createOrder(@RequestBody OrderCreateRequestDto orderCreateRequestDto) {
//        orderService.createOrder(orderCreateRequestDto);
//        return new ResponseEntity<String>(
//                "Order successfully created",
//                HttpStatus.CREATED
//        );
//    }
    @PostMapping("/api/v1/order")
    public ResponseEntity<OrderSessionResponseDto> createOrderSession(
            @CookieValue(name = "guestId") UUID guestId,
            @CookieValue(name = "orderType") OrderType orderType,
            Authentication authentication
    ) {
        OrderSession orderSession = orderService.createOrderSession(guestId, authentication, orderType);
        OrderSessionResponseDto orderSessionResponseDto = orderSessionResponseDtoMapper.toOrderSessionResponseDto(orderSession);
        return new ResponseEntity<>(
                orderSessionResponseDto,
                HttpStatus.CREATED
        );
    }

    @GetMapping("/api/v1/order")
    public ResponseEntity<OrderSessionResponseDto> loadOrderSession(
            @CookieValue(name = "guestId") UUID guestId,
            Authentication authentication
    ) {
        OrderSession orderSession = orderService.loadOrderSession(guestId, authentication);
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
//                orderResponseDtoMapper.toOrderResponseDto(order)
//        );
//    }

    @GetMapping("/api/v1/order/checkoutSession/{sessionId}")
    public ResponseEntity<OrderResponseDto> loadOrderByCheckoutSessionId(
            @PathVariable(name = "sessionId") String checkoutSessionId,
            @CookieValue(name = "guestId") UUID guestId,
            Authentication authentication
    ) {
        log.info("Checkout called {}", checkoutSessionId);
        Order order = orderService.loadOrderByCheckoutSessionId(checkoutSessionId);
        return ResponseEntity.ok(
                orderResponseDtoMapper.toOrderResponseDto(order)
        );
    }
}
