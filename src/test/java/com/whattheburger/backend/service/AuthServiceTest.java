package com.whattheburger.backend.service;

import com.whattheburger.backend.service.dto.JwtDto;
import com.whattheburger.backend.util.JwtTokenUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    JwtTokenUtil jwtTokenUtil;
    @InjectMocks
    AuthService authService;

    @Test
    public void givenProperCredential_whenAuthenticate_thenSuccess() throws Exception {
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(jwtTokenUtil.createAccessToken(anyString())).thenReturn("access-token");
        when(jwtTokenUtil.createRefreshToken(anyString())).thenReturn("refresh-token");

        JwtDto jwtDto = authService.authenticate("test@example.com", "password");

        Assertions.assertNotNull(jwtDto);
        Assertions.assertEquals("access-token", jwtDto.getAccessToken());
        Assertions.assertEquals("refresh-token", jwtDto.getRefreshToken());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(authentication, times(2)).getName();
        verify(jwtTokenUtil).createAccessToken(anyString());
        verify(jwtTokenUtil).createRefreshToken(anyString());
    }

    @Test
    public void givenWrongCredential_whenAuthenticate_thenThrowException() throws Exception {

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(
                new BadCredentialsException("Bad credentials")
        );

        Assertions.assertThrows(BadCredentialsException.class, () -> authService.authenticate("test@example.com", "password"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}
