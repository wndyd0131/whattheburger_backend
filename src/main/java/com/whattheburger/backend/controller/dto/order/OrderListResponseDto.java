package com.whattheburger.backend.controller.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
@AllArgsConstructor
@Builder
public class OrderListResponseDto {
    private Long orderCount;
    private List<OrderDto> orders;
}
