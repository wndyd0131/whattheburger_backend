package com.whattheburger.backend.controller;

import com.whattheburger.backend.controller.dto.cart.CartRequestDto;
import com.whattheburger.backend.controller.dto.cart.CartResponseDto;
import com.whattheburger.backend.controller.mapper.CartMapper;
import com.whattheburger.backend.domain.Cart;
import com.whattheburger.backend.domain.CartList;
import com.whattheburger.backend.service.CartService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Tag(name = "Cart")
@Slf4j
public class CartController {

    private final CartService cartService;
    private final CartMapper cartMapper;

    @PostMapping("/api/v1/cart")
    public ResponseEntity<String> addToCart(HttpServletRequest request, @RequestBody CartRequestDto cartRequestDto) {
        HttpSession session = request.getSession(true);
        String sessionId = session.getId();
        log.info("SESSION_ID: {}", sessionId);
        cartService.saveCart(sessionId, cartRequestDto);
        return new ResponseEntity<>(
                "Order successfully saved",
                HttpStatus.CREATED
        );
    }

    @GetMapping("/api/v1/cart")
    public ResponseEntity<List<CartResponseDto>> loadCart(HttpSession session) {
        String sessionId = session.getId();
        log.info("SESSION_ID: {}", sessionId);

        CartList cartList = cartService.loadCart(sessionId);

        List<CartResponseDto> cartResponseDtos = cartList.getCarts()
                .stream()
                .map(cartMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(cartResponseDtos);
    }
}
