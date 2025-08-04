package com.whattheburger.backend.controller;

import com.whattheburger.backend.controller.dto.cart.CartModifyRequestDto;
import com.whattheburger.backend.controller.dto.cart.CartRequestDto;
import com.whattheburger.backend.controller.dto.cart.CartResponseDto;
import com.whattheburger.backend.controller.dto.cart.ProductResponseDto;
import com.whattheburger.backend.controller.dto_mapper.CartResponseDtoMapper;
import com.whattheburger.backend.service.CartService;
import com.whattheburger.backend.service.dto.cart.ProcessedCartDto;
import com.whattheburger.backend.service.dto.cart.ProcessedProductDto;
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
    private final CartResponseDtoMapper cartResponseDtoMapper;

    @PostMapping("/api/v1/cart")
    public ResponseEntity<CartResponseDto> addToCart(
            @RequestBody CartRequestDto cartRequestDto,
            @CookieValue(name = "guestId") String guestId,
            Authentication authentication
            ) {
        log.info("GUEST_ID: {}", guestId);
        ProcessedCartDto processedCartDto = cartService.saveCart(guestId, authentication, cartRequestDto);
        CartResponseDto cartResponseDto = cartResponseDtoMapper.toCartResponseDto(processedCartDto);
        return new ResponseEntity<>(
                cartResponseDto,
                HttpStatus.CREATED
        );
    }

    @GetMapping("/api/v1/cart")
    public ResponseEntity<CartResponseDto> loadCart(
            @CookieValue(name = "guestId") String guestId,
            Authentication authentication
    ) {
        log.info("GUEST_ID: {}", guestId);

        ProcessedCartDto processedCartDto = cartService.loadCart(guestId, authentication); // let other services reuse product entity
        CartResponseDto cartResponseDto = cartResponseDtoMapper.toCartResponseDto(processedCartDto);

        return ResponseEntity.ok(cartResponseDto);
    }

    @GetMapping("/api/v1/cart/{idx}")
    public ResponseEntity<ProductResponseDto> loadItemByIdx(
            @PathVariable("idx") int cartIdx,
            @CookieValue(name = "guestId") String guestId,
            Authentication authentication
    ) {
        ProcessedProductDto processedProductDto = cartService.loadCartByIdx(guestId, cartIdx, authentication);
        ProductResponseDto productResponseDto = cartResponseDtoMapper.toProductResponse(processedProductDto);
        return ResponseEntity.ok(
                productResponseDto
        );
    }

    @PatchMapping("/api/v1/cart/{idx}")
    public ResponseEntity<CartResponseDto> modifyItemByIdx(
            @RequestBody CartModifyRequestDto cartRequestDto,
            @PathVariable("idx") int cartIdx,
            @CookieValue(name = "guestId") String guestId,
            Authentication authentication
    ) {
        ProcessedCartDto processedCartDto = cartService.modifyItem(guestId, cartIdx, cartRequestDto, authentication);
        CartResponseDto cartResponseDto = cartResponseDtoMapper.toCartResponseDto(processedCartDto);
        return ResponseEntity.ok(
                cartResponseDto
        );
    }

    @DeleteMapping("/api/v1/cart/{idx}")
    public ResponseEntity<CartResponseDto> removeItemByIdx(
            @PathVariable("idx") int cartIdx,
            @CookieValue(name = "guestId") String guestId,
            Authentication authentication
    ) {
        ProcessedCartDto processedCartDto = cartService.deleteItem(guestId, cartIdx, authentication);
        CartResponseDto cartResponseDto = cartResponseDtoMapper.toCartResponseDto(processedCartDto);
        return ResponseEntity.ok(
                cartResponseDto
        );
    }
}
