package com.whattheburger.backend.domain.order;

import java.util.UUID;

public record GuestKey(UUID guestId) implements OrderOwnerKey {}