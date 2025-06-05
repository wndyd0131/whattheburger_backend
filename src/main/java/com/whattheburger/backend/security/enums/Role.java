package com.whattheburger.backend.security.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public enum Role {
    USER("ROLE_USER"),
    EMPLOYER("ROLE_EMPLOYER,ROLE_USER"),
    ADMIN("ROLE_ADMIN,ROLE_EMPLOYER,ROLE_USER");

    private final String roles;
}
