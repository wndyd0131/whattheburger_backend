package com.whattheburger.backend.controller;

import com.whattheburger.backend.controller.dto.cart.*;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Cart")
@Slf4j
public class CartController {

    private final CartService cartService;
    private final CartResponseDtoMapper cartResponseDtoMapper;

    @PostMapping("/api/v1/store/{storeId}/cart")
    public ResponseEntity<CartResponseDto> addToCart(
            @PathVariable(name = "storeId") Long storeId,
            @RequestBody CartCreateRequestDto cartRequestDto,
            @CookieValue(name = "guestId", required = false) UUID guestId,
            Authentication authentication
            ) {
        log.info("GUEST_ID: {}", guestId);
        ProcessedCartDto processedCartDto = cartService.saveCart(storeId, guestId, authentication, cartRequestDto);
        CartResponseDto cartResponseDto = cartResponseDtoMapper.toCartResponseDto(processedCartDto);
        return new ResponseEntity<>(
                cartResponseDto,
                HttpStatus.CREATED
        );
    }

    @GetMapping("/api/v1/store/{storeId}/cart")
    public ResponseEntity<CartResponseDto> loadCart(
            @PathVariable(name = "storeId") Long storeId,
            @CookieValue("guestId") UUID guestId,
            Authentication authentication
    ) {
        log.info("GUEST_ID: {}", guestId);

        ProcessedCartDto processedCartDto = cartService.loadCart(storeId, guestId, authentication); // let other services reuse product entity
        CartResponseDto cartResponseDto = cartResponseDtoMapper.toCartResponseDto(processedCartDto);

        return ResponseEntity.ok(cartResponseDto);
    }

    @GetMapping("/api/v1/store/{storeId}/cart/{cartIdx}")
    public ResponseEntity<ProductResponseDto> loadItemByIdx(
            @PathVariable(name = "cartIdx") int cartIdx,
            @PathVariable(name = "storeId") Long storeId,
            @CookieValue(name = "guestId") UUID guestId,
            Authentication authentication
    ) {
        ProcessedProductDto processedProductDto = cartService.loadCartByIdx(storeId, guestId, cartIdx, authentication);
        ProductResponseDto productResponseDto = cartResponseDtoMapper.toProductResponse(processedProductDto);
        return ResponseEntity.ok(
                productResponseDto
        );
    }

    @PatchMapping("/api/v1/store/{storeId}/cart")
    public ResponseEntity<CartResponseDto> mergeCart(
            @PathVariable(name = "storeId") Long storeId,
            @CookieValue(name = "guestId") UUID guestId,
            Authentication authentication
    ) {
        cartService.mergeCart(storeId, guestId, authentication);
        return null;
    }

    @PatchMapping("/api/v1/store/{storeId}/cart/{cartIdx}/option")
    public ResponseEntity<CartResponseDto> modifyOptionsByIdx(
            @RequestBody CartOptionModifyRequestDto cartRequestDto,
            @PathVariable(name = "storeId") Long storeId,
            @PathVariable("cartIdx") int cartIdx,
            @CookieValue(name = "guestId") UUID guestId,
            Authentication authentication
    ) {
        List<CustomRuleRequest> customRuleRequests = cartRequestDto.getCustomRuleRequests();
        ProcessedCartDto processedCartDto = cartService.modifyItem(storeId, guestId, cartIdx, customRuleRequests, authentication);
        CartResponseDto cartResponseDto = cartResponseDtoMapper.toCartResponseDto(processedCartDto);
        return ResponseEntity.ok(
                cartResponseDto
        );
    }

    @PatchMapping("/api/v1/store/{storeId}/cart/{cartIdx}/product")
    public ResponseEntity<CartResponseDto> modifyProductByIdx(
            @RequestBody CartProductModifyRequestDto cartRequestDto,
            @PathVariable(name = "storeId") Long storeId,
            @PathVariable(name = "cartIdx") int cartIdx,
            @CookieValue(name = "guestId") UUID guestId,
            Authentication authentication
    ) {
        int quantity = cartRequestDto.getQuantity();
        ProcessedCartDto processedCartDto = cartService.modifyItemQuantity(storeId, guestId, cartIdx, quantity, authentication);
        CartResponseDto cartResponseDto = cartResponseDtoMapper.toCartResponseDto(processedCartDto);
        return ResponseEntity.ok(
                cartResponseDto
        );
    }

    @DeleteMapping("/api/v1/store/{storeId}/cart/{cartIdx}")
    public ResponseEntity<CartResponseDto> removeItemByIdx(
            @PathVariable(name = "storeId") Long storeId,
            @PathVariable(name = "cartIdx") int cartIdx,
            @CookieValue(name = "guestId") UUID guestId,
            Authentication authentication
    ) {
        ProcessedCartDto processedCartDto = cartService.deleteItem(storeId, guestId, cartIdx, authentication);
        CartResponseDto cartResponseDto = cartResponseDtoMapper.toCartResponseDto(processedCartDto);
        return ResponseEntity.ok(
                cartResponseDto
        );
    }

    @DeleteMapping("/api/v1/store/{storeId}/cart")
    public ResponseEntity<CartResponseDto> removeAllItemByIdx(
            @PathVariable(name = "storeId") Long storeId,
            @CookieValue(name = "guestId") UUID guestId,
            Authentication authentication
    ) {
        ProcessedCartDto processedCartDto = cartService.deleteAllItem(storeId, guestId, authentication);
        CartResponseDto cartResponseDto = cartResponseDtoMapper.toCartResponseDto(processedCartDto);
        return ResponseEntity.ok(
                cartResponseDto
        );
    }
}
