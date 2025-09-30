package com.staticdata.platform.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Utility Class for generating, validating and parsing JWT tokens
 */
@Component
@Slf4j
public class JwtUtils {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private int jwtExpirationMs;

    /**
     * Find byUserAuthenticationInformationGenerateJWT token
     */
    public String generateJwtToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        return generateJwtToken(userPrincipal.getUsername());
    }

    /**
     * Find byUsernameGenerateJWT token
     */
    public String generateJwtToken(String username) {
        Date expiryDate = new Date((new Date()).getTime() + jwtExpirationMs);
        
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Get username from JWT token
     */
    public String getUsernameFromJwtToken(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * ValidateJWT tokenYesNoValid
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(authToken);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            log.error("JWT validation failed: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Check if token is about to expire (remaining time less than 30 minutes)
     */
    public boolean isTokenExpiringSoon(String token) {
        try {
            Claims claims = Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
            
            Date expiration = claims.getExpiration();
            Date now = new Date();
            long timeUntilExpiration = expiration.getTime() - now.getTime();
            
            // If remaining time less than 30 minutes (1800000 milliseconds), return true
            return timeUntilExpiration < 1800000;
        } catch (Exception e) {
            log.error("Error checking token expiration: {}", e.getMessage());
            return true; // If cannot parse, set as about to expire
        }
    }

    /**
     * RefreshJWT token
     */
    public String refreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
            
            String username = claims.getSubject();
            return generateJwtToken(username);
        } catch (Exception e) {
            log.error("Error refreshing token: {}", e.getMessage());
            throw new RuntimeException("Invalid token for refresh");
        }
    }

    /**
     * Get remaining valid time of token (milliseconds)
     */
    public long getTokenRemainingTime(String token) {
        try {
            Claims claims = Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
            
            Date expiration = claims.getExpiration();
            Date now = new Date();
            return expiration.getTime() - now.getTime();
        } catch (Exception e) {
            log.error("Error getting token remaining time: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Get JWT expiration time (milliseconds)
     */
    public long getJwtExpirationMs() {
        return jwtExpirationMs;
    }

    /**
     * GetSignatureKey
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}