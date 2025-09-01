package com.whattheburger.backend.domain.order;

import java.util.UUID;

public record GuestPreviewKey(UUID guestId) implements OrderPreviewOwnerKey {}