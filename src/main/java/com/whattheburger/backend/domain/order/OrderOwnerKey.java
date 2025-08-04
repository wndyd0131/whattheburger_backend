package com.whattheburger.backend.domain.order;

public sealed interface OrderOwnerKey permits UserKey, GuestKey {
}
