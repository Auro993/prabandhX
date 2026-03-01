package com.prabandhx.prabandhx.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final String SECRET =
            "mysecretkeymysecretkeymysecretkeymysecretkey";

    private final long EXPIRATION = 1000 * 60 * 60 * 24;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    // ========================
    // Extract Email
    // ========================
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    // ========================
    // Extract Role
    // ========================
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    // ========================
    // Extract Expiration
    // ========================
    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ========================
    // GENERATE TOKEN (UPDATED)
    // ========================
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)   // 🔥 ADD ROLE HERE
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + EXPIRATION)
                )
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ========================
    // VALIDATE TOKEN
    // ========================
    public boolean validateToken(String token,
                                 UserDetails userDetails) {

        final String email = extractEmail(token);

        return (email.equals(userDetails.getUsername())
                && !isTokenExpired(token));
    }
}