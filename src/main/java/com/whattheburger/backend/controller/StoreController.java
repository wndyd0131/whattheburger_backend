package com.whattheburger.backend.controller;

import com.whattheburger.backend.controller.dto.store.NearByStoreReadResponseDto;
import com.whattheburger.backend.controller.dto.store.StoreReadResponse;
import com.whattheburger.backend.domain.Address;
import com.whattheburger.backend.domain.Coordinate;
import com.whattheburger.backend.domain.Store;
import com.whattheburger.backend.service.StoreService;
import com.whattheburger.backend.service.dto.NearByStoreDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StoreController {
    private final StoreService storeService;

    @GetMapping("/api/v1/store")
    public ResponseEntity<List<StoreReadResponse>> getAllStores() {
        List<Store> stores = storeService.findAllStores();
        List<StoreReadResponse> storeReadResponses = stores.stream()
                .map(store -> StoreReadResponse
                        .builder()
                        .storeId(store.getId())
                        .overpassId(store.getOverpassId())
                        .houseNumber(store.getHouseNumber())
                        .build())
                .toList();
        return ResponseEntity.ok(
                storeReadResponses
        );
    }

    @GetMapping("/api/v1/store/nearby")
    public ResponseEntity<List<NearByStoreReadResponseDto>> getStoresNearBy(
            @RequestParam(name = "lon") Double lon,
            @RequestParam(name = "lat") Double lat,
            @RequestParam(name = "radiusMeter") Double radiusMeter
    ) {
        List<NearByStoreDto> storeDtos = storeService.findStoresNearBy(lon, lat, radiusMeter);
        return ResponseEntity.ok(
                storeDtos.stream()
                        .map(storeDto -> {
                            Store store = storeDto.getStore();
                            Address address = store.getAddress();
                            Coordinate coordinate = store.getCoordinate();
                            return NearByStoreReadResponseDto
                                    .builder()
                                    .address(NearByStoreReadResponseDto.AddressDto
                                            .builder()
                                            .city(address.getCity())
                                            .state(address.getState())
                                            .street(address.getStreet())
                                            .zipcode(address.getZipcode())
                                            .build()
                                    )
                                    .coordinate(NearByStoreReadResponseDto.CoordinateDto
                                            .builder()
                                            .latitude(coordinate.getLatitude())
                                            .longitude(coordinate.getLongitude())
                                            .build()
                                    )
                                    .houseNumber(store.getHouseNumber())
                                    .phoneNum(store.getPhoneNum())
                                    .storeId(store.getId())
                                    .branch(store.getBranch())
                                    .distance(storeDto.getDistance())
                                    .duration(storeDto.getDuration())
                                    .build();
                        })
                        .toList()
        );
    }
}
