package com.whattheburger.backend.controller.dto.store;

import lombok.*;

import java.time.LocalTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class StoreReadResponse {
    private Long storeId;
    private Long overpassId;
    private Long houseNumber;
    private LocalTime openTime;
    private LocalTime closeTime;
    private String branch;
    private NearByStoreReadResponseDto.AddressDto address;
    private NearByStoreReadResponseDto.CoordinateDto coordinate;
    private String phoneNum;
    private String website;
}
