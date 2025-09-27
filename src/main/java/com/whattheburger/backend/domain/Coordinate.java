package com.whattheburger.backend.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public class Coordinate {
    private Double latitude;
    private Double longitude;
}
