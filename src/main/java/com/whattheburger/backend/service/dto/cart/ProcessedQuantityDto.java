package com.whattheburger.backend.service.dto.cart;

import com.whattheburger.backend.domain.ProductOptionOptionQuantity;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class ProcessedQuantityDto {
    private List<ProductOptionOptionQuantity> productOptionOptionQuantities;
    private ProductOptionOptionQuantity selectedQuantity;
}
