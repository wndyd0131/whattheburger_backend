package com.whattheburger.backend.controller.dto.store;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class NearByStoreReadResponseDto {
    private Long storeId;
    private Long overpassId;
    private Long houseNumber;
    private LocalTime openTime;
    private LocalTime closeTime;
    private String branch;
    private AddressDto address;
    private CoordinateDto coordinate;
    private String phoneNum;
    private String website;
    private Double distance;
    private Double duration;

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class CoordinateDto {
        private Double longitude;
        private Double latitude;
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class AddressDto {
        private String city;
        private String street;
        private String state;
        private String zipcode;
    }

}
