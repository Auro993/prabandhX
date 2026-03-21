package com.prabandhx.prabandhx.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // Secret key (should be long for HS256)
    private static final String SECRET =
            "mysecretkeymysecretkeymysecretkeymysecretkey";

    // Token expiration - 30 days (30 * 24 * 60 * 60 * 1000)
    private static final long EXPIRATION = 1000L * 60 * 60 * 24 * 30;

    // =========================
    // GET SIGNING KEY
    // =========================
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    // =========================
    // GENERATE TOKEN
    // =========================
    public String generateToken(String email, String role) {
        Date now = new Date();
        Date expiryDate = new Date(System.currentTimeMillis() + EXPIRATION);
        
        System.out.println("JWT Generated - Email: " + email);
        System.out.println("JWT Generated - Issued at: " + now);
        System.out.println("JWT Generated - Expires at: " + expiryDate);
        System.out.println("JWT Generated - Valid for: 30 days");

        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // =========================
    // EXTRACT EMAIL
    // =========================
    public String extractEmail(String token) {
        try {
            return extractAllClaims(token).getSubject();
        } catch (ExpiredJwtException e) {
            System.err.println("JWT expired: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Error extracting email: " + e.getMessage());
            throw e;
        }
    }

    // =========================
    // EXTRACT ROLE
    // =========================
    public String extractRole(String token) {
        try {
            return extractAllClaims(token).get("role", String.class);
        } catch (ExpiredJwtException e) {
            System.err.println("JWT expired while extracting role: " + e.getMessage());
            throw e;
        }
    }

    // =========================
    // EXTRACT EXPIRATION
    // =========================
    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    // =========================
    // CHECK TOKEN EXPIRATION
    // =========================
    private boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    // =========================
    // VALIDATE TOKEN
    // =========================
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String email = extractEmail(token);
            boolean isValid = (email.equals(userDetails.getUsername()) && !isTokenExpired(token));
            
            if (!isValid) {
                System.out.println("Token validation failed - either email mismatch or expired");
            }
            
            return isValid;
        } catch (ExpiredJwtException e) {
            System.out.println("Token validation failed - Token expired: " + e.getMessage());
            return false;
        } catch (SignatureException e) {
            System.out.println("Token validation failed - Invalid signature: " + e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            System.out.println("Token validation failed - Malformed token: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println("Token validation failed - " + e.getMessage());
            return false;
        }
    }

    // =========================
    // CHECK IF TOKEN IS EXPIRED (NO EXCEPTION)
    // =========================
    public boolean isTokenExpiredGracefully(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    // =========================
    // EXTRACT ALL CLAIMS WITH GRACEFUL ERROR HANDLING
    // =========================
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // Return expired claims anyway for debugging
            return e.getClaims();
        }
    }
}