package com.whattheburger.backend.controller;

import com.whattheburger.backend.controller.dto.LoginRequestDto;
import com.whattheburger.backend.controller.dto.LoginResponseDto;
import com.whattheburger.backend.controller.exception.UserNotFoundException;
import com.whattheburger.backend.service.AuthService;
import com.whattheburger.backend.service.dto.JwtDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/api/v1/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        try {
            JwtDto jwtDto = authService.authenticate(loginRequestDto.getEmail(), loginRequestDto.getPassword());
            String accessToken = jwtDto.getAccessToken();
            String refreshToken = jwtDto.getRefreshToken();
            return new ResponseEntity<>(
                    new LoginResponseDto(accessToken, refreshToken),
                    HttpStatusCode.valueOf(200)
            );
        }
        catch (UsernameNotFoundException e) {
            throw new UserNotFoundException();
        }
        catch (BadCredentialsException e) {
            throw new com.whattheburger.backend.controller.exception.BadCredentialsException();
        }

    }
}
