package com.whattheburger.backend.service;

import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.matrix.v1.MapboxMatrix;
import com.mapbox.api.matrix.v1.models.MatrixResponse;
import com.mapbox.geojson.Point;
import com.whattheburger.backend.service.dto.MapboxResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class MapboxService {
    @Value("${mapbox.accessToken}")
    private String accessToken;

    public MapboxMatrix createMatrix(List<Point> coordinates) {
        return MapboxMatrix
                .builder()
                .accessToken(accessToken)
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .destinations(0)
                .coordinates(coordinates)
                .build();
    }

    public MapboxResponse getSinglePathResponses(List<Point> coordinates) {
        MapboxMatrix mapboxMatrix = MapboxMatrix
                .builder()
                .accessToken(accessToken)
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .addAnnotations("duration","distance")
                .coordinates(coordinates)
                .build();

        List<Double> distances = new ArrayList<>();
        distances.add(0D);
        List<Double> durations = new ArrayList<>();
        durations.add(0D);

        try {
            Response<MatrixResponse> response = mapboxMatrix.cloneCall().execute();

            MatrixResponse body = response.body();
            log.info("Matrix API call succeeded!");
            log.info("Code: {}", response.code());
            log.info("Coordinates: {}", coordinates.size());
            log.info("Distances: {}", body.distances().size());
            log.info("Durations: {}", body.durations().size());

            List<Double[]> distanceMatrix = body.distances();
            for (int i = 1; i < distanceMatrix.size(); i++) {
                distances.add(distanceMatrix.get(i)[0]);
            }
            System.out.println(distances);

            List<Double[]> durationMatrix = body.durations();
            for (int i = 1; i < durationMatrix.size(); i++) {
                durations.add(durationMatrix.get(i)[0]);
            }
            System.out.println(durations);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return MapboxResponse
                .builder()
                .distances(distances)
                .durations(durations)
                .build();
    }
}
