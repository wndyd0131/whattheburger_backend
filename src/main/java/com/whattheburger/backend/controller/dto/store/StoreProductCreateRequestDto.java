package com.whattheburger.backend.controller.dto.store;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class StoreProductCreateRequestDto {
    private List<Long> storeIds;
    private Long productId;
}
