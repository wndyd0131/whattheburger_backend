package com.whataburger.whataburgerproject.controller;

import com.whataburger.whataburgerproject.controller.dto.UserCreateResponseDto;
import com.whataburger.whataburgerproject.controller.dto.UserDto;
import com.whataburger.whataburgerproject.controller.dto.UserReadResponseDTO;
import com.whataburger.whataburgerproject.exception.AuthenticationCredentialsNotFoundException;
import com.whataburger.whataburgerproject.exception.ExceptionResponse;
import com.whataburger.whataburgerproject.exception.UserPrincipalNotFoundException;
import com.whataburger.whataburgerproject.service.UserService;
import com.whataburger.whataburgerproject.service.dto.AuthUserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/api/v1/users")
    public ResponseEntity<UserReadResponseDTO> getUser() {
        // service
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AuthUserDto authUserDto = userService.getAuthenticatedUser(authentication);
        return new ResponseEntity<>(
                new UserReadResponseDTO(
                        authUserDto.getFirstName(),
                        authUserDto.getLastName(),
                        authUserDto.getPhoneNum(),
                        authUserDto.getEmail(),
                        authUserDto.getZipcode()
                ),
                HttpStatusCode.valueOf(200)
        );
    }

    @PostMapping("/api/v1/register")
    public ResponseEntity<UserCreateResponseDto> saveUser(@RequestBody UserDto userDto) {
        Long userId = userService.join(userDto.toEntity());
        return new ResponseEntity<>(
                new UserCreateResponseDto(userId),
                HttpStatusCode.valueOf(201)
        );
    }

    @ExceptionHandler(UserPrincipalNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleUserPrincipalNotFoundException(UserPrincipalNotFoundException ex) {
        return new ResponseEntity<>(
                new ExceptionResponse(
                        ex.getMessage(),
                        HttpStatus.UNAUTHORIZED.value()
                ),
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleAuthenticationCredentialsNotFoundException(AuthenticationCredentialsNotFoundException ex) {
        return new ResponseEntity<>(
                new ExceptionResponse(
                        ex.getMessage(),
                        HttpStatus.UNAUTHORIZED.value()
                ),
                HttpStatus.UNAUTHORIZED
        );
    }
}
