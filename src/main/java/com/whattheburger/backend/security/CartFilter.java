package com.whattheburger.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class CartFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Cookie[] cookies = request.getCookies();
        boolean hasCartId = false;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("cartId".equals(cookie.getName())) {
                    hasCartId = true;
                    break;
                }
            }
        }

        if (!hasCartId) {
            String cartId = UUID.randomUUID().toString();
            Cookie cookie = new Cookie("cartId", cartId);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(60 * 60 * 24 * 30);
            response.addCookie(cookie);
        }

        filterChain.doFilter(request, response);
    }
}
