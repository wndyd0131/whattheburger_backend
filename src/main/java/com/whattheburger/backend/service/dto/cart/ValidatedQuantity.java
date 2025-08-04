package com.whattheburger.backend.service.dto.cart;

import com.whattheburger.backend.domain.ProductOptionOptionQuantity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ValidatedQuantity {
    private List<ProductOptionOptionQuantity> productOptionOptionQuantities;
    private ProductOptionOptionQuantity selectedQuantity;
}
