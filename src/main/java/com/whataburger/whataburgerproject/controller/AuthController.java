package com.whataburger.whataburgerproject.controller;

import com.whataburger.whataburgerproject.controller.dto.LoginRequestDto;
import com.whataburger.whataburgerproject.controller.dto.LoginResponseDto;
import com.whataburger.whataburgerproject.service.AuthService;
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
public class AuthController {

    private final AuthService authService;

    @PostMapping("/api/v1/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
//        JwtToken = authService.authenticate();
        return new ResponseEntity<>(
                new LoginResponseDto("", ""),
                HttpStatusCode.valueOf(200)
                );
    }
}
