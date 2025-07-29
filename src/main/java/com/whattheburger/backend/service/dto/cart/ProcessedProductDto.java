package com.whattheburger.backend.service.dto.cart;

import com.whattheburger.backend.domain.Product;
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
public class ProcessedProductDto {
    private Product product;
    private Integer quantity;
    private List<ProcessedCustomRuleDto> processedCustomRuleDtos;
    private BigDecimal calculatedProductPrice;
    private BigDecimal calculatedExtraPrice;
}
