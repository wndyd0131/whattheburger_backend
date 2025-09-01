package com.whattheburger.backend.domain.order;

import com.whattheburger.backend.domain.enums.CountType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class OrderSessionOption {
    private Long productOptionId;
    private String name;
    private CountType countType;
    private BigDecimal totalPrice;
    private BigDecimal basePrice;
    private Double totalCalories;
    private Integer quantity;
    private List<OrderSessionOptionTrait> orderSessionOptionTraits;
    private QuantityDetail quantityDetail;
}
