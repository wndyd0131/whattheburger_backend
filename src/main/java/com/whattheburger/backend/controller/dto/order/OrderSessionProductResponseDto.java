package com.whattheburger.backend.controller.dto.order;

import com.whattheburger.backend.domain.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class OrderSessionProductResponseDto {
    private Long id;
    private Long productId;
    private Integer quantity;
    //    private String forWhom;
    private String name;
    private BigDecimal totalPrice;
    private BigDecimal extraPrice;
    private BigDecimal basePrice;
    private String imageSource;
    private Double calculatedCalories;
    private ProductType productType;
    private List<OrderSessionCustomRuleResponseDto> customRuleResponses;
}
