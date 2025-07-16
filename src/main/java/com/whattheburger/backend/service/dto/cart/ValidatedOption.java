package com.whattheburger.backend.service.dto.cart;

import com.whattheburger.backend.domain.ProductOption;
import com.whattheburger.backend.domain.ProductOptionTrait;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ValidatedOption {
    private ProductOption productOption;
    private List<ValidatedTrait> validatedTraits;
    private Boolean isSelected;
    private Integer quantity;
    private ValidatedQuantity validatedQuantity;
}
