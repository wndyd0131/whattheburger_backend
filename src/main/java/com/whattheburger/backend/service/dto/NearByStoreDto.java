package com.whattheburger.backend.service.dto;

import com.whattheburger.backend.domain.Store;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class NearByStoreDto {
    private Store store;
    private Double distance;
    private Double duration;
}
