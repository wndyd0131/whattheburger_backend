package com.whattheburger.backend.domain.order;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class OrderSessionCustomRule {
    private Long customRuleId;
    private String name;
    private BigDecimal totalPrice;
    private List<OrderSessionOption> orderSessionOptions = new ArrayList<>();
}
