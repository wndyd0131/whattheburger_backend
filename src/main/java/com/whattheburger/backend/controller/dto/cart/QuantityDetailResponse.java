package com.whattheburger.backend.controller.dto.cart;

import com.whattheburger.backend.domain.enums.QuantityType;
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
public class QuantityDetailResponse {
    private List<QuantityDetail> quantityDetails;
    private Long selectedId;
    // may need orderIndex
}
