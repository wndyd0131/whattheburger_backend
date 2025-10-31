package com.whattheburger.backend.security;

import com.google.common.net.HttpHeaders;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Component
@Slf4j
public class GuestFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        System.out.println(">>> " + request.getMethod() + " " + request.getRequestURI());
        Cookie[] cookies = request.getCookies();
        System.out.println(">>> Cookies: " + cookies);
        boolean hasGuestId = false;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("guestId".equals(cookie.getName())) {
                    hasGuestId = true;
                    break;
                }
            }
        }

        if (!hasGuestId) {
            String guestId = UUID.randomUUID().toString();
            ResponseCookie guestCookie = ResponseCookie.from("guestId", guestId)
                    .path("/")
                    .secure(true)
                    .httpOnly(true)
                    .sameSite("None")
                    .maxAge(Duration.ofDays(30))
                    .build();
            response.addHeader(
                    HttpHeaders.SET_COOKIE, guestCookie.toString()
            );
            log.info("Cookie[guestId] set as {}", guestId);
        }

        filterChain.doFilter(request, response);
    }
}
