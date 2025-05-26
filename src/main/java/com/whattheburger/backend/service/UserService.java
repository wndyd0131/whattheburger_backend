package com.whattheburger.backend.service;

import com.whattheburger.backend.domain.User;
import com.whattheburger.backend.security.exception.AuthenticationCredentialsNotFoundException;
import com.whattheburger.backend.security.exception.UserPrincipalNotFoundException;
import com.whattheburger.backend.repository.UserRepository;
import com.whattheburger.backend.security.UserDetailsImpl;
import com.whattheburger.backend.service.dto.AuthUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true) // optimization
@RequiredArgsConstructor // auto create constructor for field with final
public class UserService {

    private final UserRepository userRepository; // final causes compile error if there's no injection in constructor

    @Transactional
    public Long join(User user) {
        validateDuplicateEmail(user);
        userRepository.save(user);
        return user.getId();
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
            throw new AuthenticationCredentialsNotFoundException("Credentials not found", HttpStatus.UNAUTHORIZED);

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails) {
            return new AuthUserDto(
                    userDetails.getFirstName(),
                    userDetails.getLastName(),
                    userDetails.getPhoneNum(),
                    userDetails.getUsername(),
                    userDetails.getZipcode()
            );
        }
        throw new UserPrincipalNotFoundException("User principal not found", HttpStatus.NOT_FOUND);

    }
}
