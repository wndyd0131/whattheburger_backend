package com.whattheburger.backend.service.exception.user;

import com.whattheburger.backend.service.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends ResourceNotFoundException {
    public UserNotFoundException(Long userId) {
        super("User with ID[" + userId + "] not found");
    }
}
