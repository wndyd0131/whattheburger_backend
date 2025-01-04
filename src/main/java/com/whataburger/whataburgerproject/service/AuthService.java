package com.whataburger.whataburgerproject.service;

import com.whataburger.whataburgerproject.util.JwtTokenUtil;
import com.whataburger.whataburgerproject.service.dto.JwtDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
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
        if (!authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        String accessToken = jwtTokenUtil.createAccessToken(authentication.getName());
        String refreshToken = jwtTokenUtil.createRefreshToken(authentication.getName());
        return new JwtDto(accessToken, refreshToken);
    }
}
