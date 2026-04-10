package com.whattheburger.backend.domain.order;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PickupInfo {
    private Instant eta;
    private Instant arrivedTime;

    public void changeETA(Instant eta) {
        this.eta = eta;
    }
}
