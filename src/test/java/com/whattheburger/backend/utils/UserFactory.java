package com.whattheburger.backend.utils;

import com.whattheburger.backend.domain.User;
import com.whattheburger.backend.security.enums.Role;

public class UserFactory {
    public static User createUser() {
        return new User(
                "Test",
                "Man",
                "512-123-5678",
                "12345",
                "test@gmail.com",
                "12345678",
                Role.ADMIN
        );
    }
}
