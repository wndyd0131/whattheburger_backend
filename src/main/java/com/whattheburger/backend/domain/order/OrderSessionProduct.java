package com.whattheburger.backend.domain.order;

import com.whattheburger.backend.domain.enums.ProductType;
import com.whattheburger.backend.util.OptionJsonConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class OrderSessionProduct {
    private Long productId;
    private Integer quantity;
    private String forWhom;
    private String name;
    private BigDecimal totalPrice;
    private BigDecimal extraPrice;
    private BigDecimal basePrice;
    private String imageSource;
    private Double totalCalories;
    private ProductType productType;
    private List<OrderSessionCustomRule> orderSessionCustomRules;
}
