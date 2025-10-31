package com.whattheburger.backend.service.dto.cart;

import com.whattheburger.backend.domain.Product;
import com.whattheburger.backend.domain.StoreProduct;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ValidatedProduct {
    private StoreProduct storeProduct;
    private Integer quantity;
}
