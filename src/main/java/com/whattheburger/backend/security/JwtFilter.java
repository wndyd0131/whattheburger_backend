package com.whattheburger.backend.security;

import com.whattheburger.backend.security.exception.JwtException;
import com.whattheburger.backend.util.JwtTokenUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {


        String authHeader = request.getHeader("Authorization");
        String token = "";
        String email = "";

        try {
            if (authHeader != null && authHeader.startsWith("Bearer")) {
                token = authHeader.substring(7);
                email = jwtTokenUtil.extractSubject(token);
                log.info("token: {}", token);
                log.info("email: {}", email);
            }

            if (!email.isEmpty() && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email); // get user from database
                if (email.equals(userDetails.getUsername()) && jwtTokenUtil.validateToken(token)) {

                    // Set authentication to SecurityContext
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }
            }
            filterChain.doFilter(request, response); // go to next filter
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT signature.");
            authenticationEntryPoint.commence(request, response, new JwtException("Invalid JWT signature"));
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token");
            authenticationEntryPoint.commence(request, response, new JwtException("Expired JWT token"));
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token");
            authenticationEntryPoint.commence(request, response, new JwtException("Unsupported JWT token"));
        }
    }
}
