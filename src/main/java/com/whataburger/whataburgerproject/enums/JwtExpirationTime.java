package com.whataburger.whataburgerproject.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum JwtExpirationTime {
    ACCESS_TOKEN_EXPIRATION_TIME(1000L * 60 * 30),
    REFRESH_TOKEN_EXPIRATION_TIME(1000L * 60 * 60 * 24 * 7);

    private long expirationTime;
}

