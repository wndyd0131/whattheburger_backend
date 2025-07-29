package com.whattheburger.backend.service.dto.cart.calculator;

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
public class OptionCalcDetail {
    private Long productOptionId;
    private BigDecimal price;
    private Integer quantity;
    private Boolean isSelected;
    private Boolean isDefault;
    private Integer defaultQuantity;
    private QuantityCalcDetail quantityCalcDetail;
    private List<TraitCalcDetail> traitCalcDetails;
    private BigDecimal traitTotalPrice;
}
