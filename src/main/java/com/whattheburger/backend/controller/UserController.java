package com.whattheburger.backend.controller;

import com.whattheburger.backend.controller.dto.UserCreateResponseDto;
import com.whattheburger.backend.controller.dto.UserDto;
import com.whattheburger.backend.controller.dto.UserReadResponseDTO;
import com.whattheburger.backend.service.UserService;
import com.whattheburger.backend.service.dto.AuthUserDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User")
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
}
