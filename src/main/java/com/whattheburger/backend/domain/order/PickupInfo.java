package com.whattheburger.backend.domain.order;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PickupInfo {
    private LocalDateTime eta;
    private LocalDateTime arrivedTime;

    public void changeETA(LocalDateTime eta) {
        this.eta = eta;
    }
}
