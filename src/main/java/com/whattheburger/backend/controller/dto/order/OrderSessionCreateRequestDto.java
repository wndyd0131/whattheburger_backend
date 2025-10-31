package com.whattheburger.backend.controller.dto.order;

import com.whattheburger.backend.domain.enums.OrderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class OrderSessionCreateRequestDto {
    private OrderType orderType;
}
