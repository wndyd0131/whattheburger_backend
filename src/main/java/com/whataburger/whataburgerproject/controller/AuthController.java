package com.whataburger.whataburgerproject.controller;

import com.whataburger.whataburgerproject.controller.dto.LoginRequestDto;
import com.whataburger.whataburgerproject.controller.dto.LoginResponseDto;
import com.whataburger.whataburgerproject.service.AuthService;
import com.whataburger.whataburgerproject.service.dto.JwtDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
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
        JwtDto jwtDto = authService.authenticate(loginRequestDto.getEmail(), loginRequestDto.getPassword());
        String accessToken = jwtDto.getAccessToken();
        String refreshToken = jwtDto.getRefreshToken();
        return new ResponseEntity<>(
                new LoginResponseDto(accessToken, refreshToken),
                HttpStatusCode.valueOf(200)
                );
    }
}
