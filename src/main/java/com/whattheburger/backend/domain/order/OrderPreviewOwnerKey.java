package com.whattheburger.backend.domain.order;

public sealed interface OrderPreviewOwnerKey permits UserPreviewKey, GuestPreviewKey {
}
