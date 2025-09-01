package com.whattheburger.backend.domain.order;

public record GuestKey(String email) implements OrderOwnerKey {
}
