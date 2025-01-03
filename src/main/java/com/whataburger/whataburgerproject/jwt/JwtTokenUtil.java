package com.whataburger.whataburgerproject.jwt;

import com.whataburger.whataburgerproject.enums.JwtExpirationTime;
import com.whataburger.whataburgerproject.repository.UserRepository;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.util.Date;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtTokenUtil {
    private final UserRepository userRepository;
    private final Long ACCESS_TOKEN_EXPIRATION_TIME = JwtExpirationTime.ACCESS_TOKEN_EXPIRATION_TIME.getExpirationTime();
    private final Long REFRESH_TOKEN_EXPIRE_TIME = JwtExpirationTime.REFRESH_TOKEN_EXPIRATION_TIME.getExpirationTime();
    @Value("${jwt.secret}")
    private String SECRET_KEY;
    private String ALGORITHM = SignatureAlgorithm.HS512.getJcaName();

    public String createAccessToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))    // JWT token issue time
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_TIME))    // JWT token expiration time
                .signWith(new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM))   // Secret key + Algorithm to sign
                .compact(); // JWT token generation
    }

    public String createRefreshToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRE_TIME))
                .signWith(new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM))
                .compact();
    }

    public Authentication getAuthenticationFromToken(String token) {
        Claims claims = Jwts
                .parserBuilder()
                .setSigningKey(SECRET_KEY.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();

        String userId = String.valueOf(claims.getSubject());
        log.info("userId: {}", userId);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userId, token);
        log.info("authentication: {}", authentication.getPrincipal());
        return new UsernamePasswordAuthenticationToken(userId, token);
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts
                    .parserBuilder()
                    .setSigningKey(SECRET_KEY.getBytes())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT signature.");
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT token");
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT token");
        } catch (IllegalArgumentException e) {
            log.info("Invalid JWT token");
        }
        return false;
    }
}
