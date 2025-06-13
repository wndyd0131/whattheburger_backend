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

    @PostMapping("/api/v1/cart")
    public ResponseEntity<List<CartResponseDto>> addToCart(HttpSession session, @RequestBody CartRequestDto cartRequestDto) {
        String sessionId = session.getId();
        log.info("SESSION_ID: {}", sessionId);
        List<CartResponseDto> cartResponseDtos = cartService.saveCart(sessionId, cartRequestDto);
        return new ResponseEntity<>(
                cartResponseDtos,
                HttpStatus.CREATED
        );
    }

    @GetMapping("/api/v1/cart")
    public ResponseEntity<List<CartResponseDto>> loadCart(HttpSession session) {
        String sessionId = session.getId();
        log.info("SESSION_ID: {}", sessionId);

        List<CartResponseDto> cartResponseDtos = cartService.loadCart(sessionId);

        return ResponseEntity.ok(cartResponseDtos);
    }
}
