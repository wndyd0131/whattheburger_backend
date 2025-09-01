package com.whattheburger.backend.domain.order;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Embeddable
@NoArgsConstructor
@Getter
public class GuestInfo {
    private UUID guestId;

    public GuestInfo(UUID guestId) {
        this.guestId = guestId;
    }
}