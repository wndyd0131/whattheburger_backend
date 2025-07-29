package com.whattheburger.backend.controller.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CartResponseDto {
    private List<ProductResponseDto> productResponses;
    private BigDecimal cartTotalPrice;
}
