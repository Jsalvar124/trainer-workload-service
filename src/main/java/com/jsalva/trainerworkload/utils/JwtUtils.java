package com.jsalva.trainerworkload.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    private final String jwtSecret;

    public JwtUtils(@Value("${jwt.secret}") String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public String getUsernameFromJwtToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    public String getUserTypeFromJwtToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("userType", String.class);
    }

    public LocalDateTime getExpirationDateFromJwtToken(String token){
        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getExpiration()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
}
