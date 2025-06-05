package com.whattheburger.backend.service;

import com.whattheburger.backend.controller.dto.UserCreateRequestDto;
import com.whattheburger.backend.domain.User;
import com.whattheburger.backend.security.enums.Role;
import com.whattheburger.backend.security.exception.AuthenticationCredentialsNotFoundException;
import com.whattheburger.backend.security.exception.UserPrincipalNotFoundException;
import com.whattheburger.backend.repository.UserRepository;
import com.whattheburger.backend.security.UserDetailsImpl;
import com.whattheburger.backend.service.dto.AuthUserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true) // optimization
@Slf4j
@RequiredArgsConstructor // auto create constructor for field with final
public class UserService {

    private final UserRepository userRepository; // final causes compile error if there's no injection in constructor
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User join(UserCreateRequestDto userDto) {

        String password = encryptPassword(userDto.getPassword());

        User user = new User(
                userDto.getFirstName(),
                userDto.getLastName(),
                userDto.getPhoneNum(),
                userDto.getZipcode(),
                userDto.getEmail(),
                password,
                Role.USER
        );
        validateDuplicateEmail(user);
        User savedUser = userRepository.save(user);
        return savedUser;
    }

    private String encryptPassword(String password) {
        return passwordEncoder.encode(password);
    }

    private void validateDuplicateEmail(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent())
            throw new IllegalArgumentException("The user already exists");
    }

    public User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("There is no such user"));
    }

    public AuthUserDto getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated())
            throw new AuthenticationCredentialsNotFoundException("Credentials not found");

        Object principal = authentication.getPrincipal();
        System.out.println("principal = " + principal.getClass().getName());
        if (principal instanceof UserDetailsImpl userDetails) {
            return AuthUserDto
                    .builder()
                    .userId(userDetails.getUserId())
                    .firstName(userDetails.getFirstName())
                    .lastName(userDetails.getLastName())
                    .phoneNum(userDetails.getPhoneNum())
                    .email(userDetails.getUsername())
                    .zipcode(userDetails.getZipcode())
                    .role(userDetails.getRole())
                    .build();
        }
        throw new UserPrincipalNotFoundException("User principal not found");

    }
}
