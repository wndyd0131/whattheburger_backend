package com.whattheburger.backend.controller;

import com.whattheburger.backend.controller.dto.order.OrderPreviewResponseDto;
import com.whattheburger.backend.controller.dto_mapper.OrderResponseDtoMapper;
import com.whattheburger.backend.domain.enums.OrderType;
import com.whattheburger.backend.domain.order.Order;
import com.whattheburger.backend.service.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Order")
public class OrderController {

    private final OrderService orderService;
    private final OrderResponseDtoMapper orderResponseDtoMapper;

//    @PostMapping()
//    public ResponseEntity<String> createOrder(@RequestBody OrderCreateRequestDto orderCreateRequestDto) {
//        orderService.createOrder(orderCreateRequestDto);
//        return new ResponseEntity<String>(
//                "Order successfully created",
//                HttpStatus.CREATED
//        );
//    }
    @PostMapping("/api/v1/order")
    public ResponseEntity<OrderPreviewResponseDto> createOrderPreview(
            @CookieValue(name = "guestId") UUID guestId,
            @CookieValue(name = "orderType") OrderType orderType,
            Authentication authentication
    ) {
        Order orderPreview = orderService.createOrderPreview(guestId, authentication, orderType);
        OrderPreviewResponseDto orderPreviewResponseDto = orderResponseDtoMapper.toOrderResponseDto(orderPreview);
        return new ResponseEntity<>(
                orderPreviewResponseDto,
                HttpStatus.CREATED
        );
    }

    @GetMapping("/api/v1/order")
    public ResponseEntity<OrderPreviewResponseDto> loadOrderPreview(
            @CookieValue(name = "guestId") UUID guestId,
            Authentication authentication
    ) {
        Order orderPreview = orderService.loadOrderPreview(guestId, authentication);
        OrderPreviewResponseDto orderPreviewResponseDto = orderResponseDtoMapper.toOrderResponseDto(orderPreview);
        return ResponseEntity.ok(orderPreviewResponseDto);
    }
}
