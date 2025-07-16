package com.whattheburger.backend.service.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ValidatedCartDto {
    private ValidatedProduct validatedProduct;
    private List<ValidatedCustomRule> validatedCustomRules;
    private Integer quantity;
}
