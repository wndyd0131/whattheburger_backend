package com.whattheburger.backend.service;

import com.whattheburger.backend.util.JwtTokenUtil;
import com.whattheburger.backend.service.dto.JwtDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;

    public JwtDto authenticate(String email, String password) {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
            String accessToken = jwtTokenUtil.createAccessToken(authentication.getName());
            String refreshToken = jwtTokenUtil.createRefreshToken(authentication.getName());
            return new JwtDto(accessToken, refreshToken);
    }
}
