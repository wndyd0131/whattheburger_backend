package com.whattheburger.backend.service;

import com.mapbox.api.matrix.v1.MapboxMatrix;
import com.mapbox.geojson.Point;
import com.whattheburger.backend.domain.Store;
import com.whattheburger.backend.repository.StoreRepository;
import com.whattheburger.backend.service.dto.MapboxResponse;
import com.whattheburger.backend.service.dto.NearByStoreDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreService {
    private final StoreRepository storeRepository;
    private final MapboxService mapboxService;

    public List<NearByStoreDto> findStoresNearBy(Double lon, Double lat, Double radiusMeter) {
        List<Store> stores = storeRepository.findNearBy(lon, lat, radiusMeter);
        List<Point> coordinates = new ArrayList<>();
        coordinates.add(Point.fromLngLat(lon, lat));
        stores.stream()
                .forEach(store -> coordinates.add(
                        Point.fromLngLat(store.getCoordinate().getLongitude(),store.getCoordinate().getLatitude())));
        MapboxResponse mapboxResponse = mapboxService.getSinglePathResponses(coordinates);

        List<Double> distances = mapboxResponse.getDistances();
        List<Double> durations = mapboxResponse.getDurations();

        List<NearByStoreDto> nearByStoreDtos = new ArrayList<>();
        if (stores.size() == distances.size() - 1 && stores.size() == durations.size() - 1) {
            for (int i = 1; i < distances.size(); i++) {
                nearByStoreDtos.add(
                        NearByStoreDto
                                .builder()
                                .store(stores.get(i-1))
                                .distance(distances.get(i))
                                .duration(durations.get(i))
                                .build()
                );
            }
        } else {
            throw new IllegalStateException();
        }

        return nearByStoreDtos;
    }

    public List<Store> findAllStores() {
        return storeRepository.findAll();
    }
}
