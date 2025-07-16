package com.whattheburger.backend.service.dto.cart.calculator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OptionDetail {
    private Double price;
    private Integer quantity;
    private QuantityDetail quantityDetail;
    private List<TraitDetail> traitDetails;
}
