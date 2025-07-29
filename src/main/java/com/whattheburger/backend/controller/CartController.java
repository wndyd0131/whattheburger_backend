package com.whattheburger.backend.controller;

import com.whattheburger.backend.controller.dto.cart.CartModifyRequestDto;
import com.whattheburger.backend.controller.dto.cart.CartRequestDto;
import com.whattheburger.backend.controller.dto.cart.CartResponseDto;
import com.whattheburger.backend.controller.dto.cart.ProductResponseDto;
import com.whattheburger.backend.service.CartService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Cart")
@Slf4j
public class CartController {

    private final CartService cartService;

    @PostMapping("/api/v1/cart")
    public ResponseEntity<CartResponseDto> addToCart(
            @RequestBody CartRequestDto cartRequestDto,
            @CookieValue(name = "cartId") String cartId,
            Authentication authentication
            ) {
        log.info("CART_ID: {}", cartId);
        CartResponseDto cartResponseDtos = cartService.saveCart(cartId, authentication, cartRequestDto);
        return new ResponseEntity<>(
                cartResponseDtos,
                HttpStatus.CREATED
        );
    }

    @GetMapping("/api/v1/cart")
    public ResponseEntity<CartResponseDto> loadCart(
            @CookieValue(name = "cartId") String cartId,
            Authentication authentication
    ) {
        log.info("CART_ID: {}", cartId);

        CartResponseDto cartResponseDto = cartService.loadCart(cartId, authentication);

        return ResponseEntity.ok(cartResponseDto);
    }

    @GetMapping("/api/v1/cart/{idx}")
    public ResponseEntity<ProductResponseDto> loadItemByIdx(
            @PathVariable("idx") int cartIdx,
            @CookieValue(name = "cartId") String cartId,
            Authentication authentication
    ) {
        ProductResponseDto productResponse = cartService.loadCartByIdx(cartId, cartIdx, authentication);
        return ResponseEntity.ok(
                productResponse
        );
    }

    @PatchMapping("/api/v1/cart/{idx}")
    public ResponseEntity<CartResponseDto> modifyItemByIdx(
            @RequestBody CartModifyRequestDto cartRequestDto,
            @PathVariable("idx") int cartIdx,
            @CookieValue(name = "cartId") String cartId,
            Authentication authentication
    ) {
        CartResponseDto cartResponseDto = cartService.modifyItem(cartId, cartIdx, cartRequestDto, authentication);
        return ResponseEntity.ok(
                cartResponseDto
        );
    }

    @DeleteMapping("/api/v1/cart/{idx}")
    public ResponseEntity<CartResponseDto> removeItemByIdx(
            @PathVariable("idx") int cartIdx,
            @CookieValue(name = "cartId") String cartId,
            Authentication authentication
    ) {
        CartResponseDto cartResponseDto = cartService.deleteItem(cartId, cartIdx, authentication);
        return ResponseEntity.ok(
                cartResponseDto
        );
    }
}
