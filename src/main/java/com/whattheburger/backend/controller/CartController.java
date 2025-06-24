package com.whattheburger.backend.controller;

import com.whattheburger.backend.controller.dto.cart.CartModifyRequestDto;
import com.whattheburger.backend.controller.dto.cart.CartRequestDto;
import com.whattheburger.backend.controller.dto.cart.CartResponseDto;
import com.whattheburger.backend.service.CartService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Cart")
@Slf4j
public class CartController {

    private final CartService cartService;

    @PostMapping("/api/v1/cart")
    public ResponseEntity<List<CartResponseDto>> addToCart(
            @RequestBody CartRequestDto cartRequestDto,
            @CookieValue(name = "cartId") String cartId,
            Authentication authentication
            ) {
        log.info("CART_ID: {}", cartId);
        List<CartResponseDto> cartResponseDtos = cartService.saveCart(cartId, authentication, cartRequestDto);
        return new ResponseEntity<>(
                cartResponseDtos,
                HttpStatus.CREATED
        );
    }

    @GetMapping("/api/v1/cart")
    public ResponseEntity<List<CartResponseDto>> loadCart(
            @CookieValue(name = "cartId") String cartId,
            Authentication authentication
    ) {
        log.info("CART_ID: {}", cartId);

        List<CartResponseDto> cartResponseDtos = cartService.loadCart(cartId, authentication);

        return ResponseEntity.ok(cartResponseDtos);
    }

    @PatchMapping("/api/v1/cart/{idx}")
    public ResponseEntity<List<CartResponseDto>> modifyItem(
            @RequestBody CartModifyRequestDto cartRequestDto,
            @PathVariable("idx") int cartIdx,
            @CookieValue(name = "cartId") String cartId,
            Authentication authentication
    ) {
        List<CartResponseDto> cartResponseDtos = cartService.modifyItem(cartId, cartIdx, cartRequestDto, authentication);
        return ResponseEntity.ok(
                cartResponseDtos
        );
    }

    @DeleteMapping("/api/v1/cart/{idx}")
    public ResponseEntity<List<CartResponseDto>> removeItem(
            @PathVariable("idx") int cartIdx,
            @CookieValue(name = "cartId") String cartId,
            Authentication authentication
    ) {
        List<CartResponseDto> cartResponseDtos = cartService.deleteItem(cartId, cartIdx, authentication);
        return ResponseEntity.ok(
                cartResponseDtos
        );
    }
}
