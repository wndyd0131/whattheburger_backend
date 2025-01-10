package com.whataburger.whataburgerproject.util;

import com.whataburger.whataburgerproject.security.enums.JwtExpirationTime;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtTokenUtil {
    private final Long ACCESS_TOKEN_EXPIRATION_TIME = JwtExpirationTime.ACCESS_TOKEN_EXPIRATION_TIME.getExpirationTime();
    private final Long REFRESH_TOKEN_EXPIRE_TIME = JwtExpirationTime.REFRESH_TOKEN_EXPIRATION_TIME.getExpirationTime();

    private final String SECRET_KEY;
    public JwtTokenUtil() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA256");
        SecretKey sk = keyGenerator.generateKey();
        SECRET_KEY = Base64.getEncoder().encodeToString(sk.getEncoded());
    }

    public String createAccessToken(String subject) {

        Map<String, Object> claims = new HashMap<>();

        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_TIME))
                .and()
                .signWith(getKey())   // Secret key + Algorithm to sign
                .compact(); // JWT token generation
    }

    public String createRefreshToken(String subject) {

        Map<String, Object> claims = new HashMap<>();

        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRE_TIME))
                .and()
                .signWith(getKey())   // Secret key + Algorithm to sign
                .compact(); // JWT token generation
    }

    public String extractSubject(String token) {
        return extractClaims(token).getSubject();
    }

    private Claims extractClaims(String token) {
        Claims claims = Jwts
                .parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims;
    }

    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

//    public Authentication getAuthenticationFromToken(String token) {
//        Claims claims = Jwts
//                .parserBuilder()
//                .setSigningKey(SECRET_KEY.getBytes())
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//
//        String userId = String.valueOf(claims.getSubject());
//        log.info("userId: {}", userId);
//        Authentication authentication = new UsernamePasswordAuthenticationToken(userId, token);
//        log.info("authentication: {}", authentication.getPrincipal());
//        return new UsernamePasswordAuthenticationToken(userId, token);
//    }

    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts
                    .parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            boolean isNotExpired = !claims.getExpiration().before(new Date());

            return isNotExpired;
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
