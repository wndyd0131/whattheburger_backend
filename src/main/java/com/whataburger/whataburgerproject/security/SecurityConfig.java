package com.whataburger.whataburgerproject.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .formLogin((auth) -> auth.disable())
//                .httpBasic((auth) -> auth.disable())
                .csrf((auth) -> auth.disable())
                .authorizeHttpRequests(
                        (auth) -> auth
                                .requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/v1/users").authenticated()
//                                .anyRequest().permitAll()
//                                .requestMatchers("/admin").hasAnyRole("ADMIN")
                );
        // Apple
        // Google
        // Facebook
        // Local

        return http.build();
    }
}
