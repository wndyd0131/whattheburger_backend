package com.whattheburger.backend.config;

import com.google.maps.GeoApiContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class GoogleApiConfig {
    @Bean
    public GeoApiContext geoApiContext(@Value("${google.apiKey}") String apiKey) {
        return new GeoApiContext.Builder()
                .apiKey(apiKey)
                .connectTimeout(5, TimeUnit.SECONDS)
                .build();
    }
}
