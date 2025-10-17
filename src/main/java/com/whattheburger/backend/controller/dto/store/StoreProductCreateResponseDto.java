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
public class StoreProductCreateResponseDto {
    private Long productId;
    private List<Long> storeIds;
}
