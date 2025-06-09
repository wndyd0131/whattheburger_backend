package com.whattheburger.backend.controller;

import com.whattheburger.backend.controller.dto.OrderCreateRequestDto;
import com.whattheburger.backend.service.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Order")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/api/v1/order")
    public ResponseEntity<String> createOrder(@RequestBody OrderCreateRequestDto orderCreateRequestDto) {
            orderService.createOrder(orderCreateRequestDto);
            return new ResponseEntity<String>(
                    "Order successfully created",
                    HttpStatus.CREATED
            );
    }
}
