package com.prabandhx.prabandhx.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtAuthenticationFilter(JwtUtil jwtUtil,
                                   UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // Skip filter for OPTIONS requests (CORS preflight)
        if (request.getMethod().equals("OPTIONS")) {
            response.setStatus(HttpServletResponse.SC_OK);
            filterChain.doFilter(request, response);
            return;
        }

        // Skip filter for auth endpoints (login, register, etc.)
        String requestPath = request.getServletPath();
        if (requestPath.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        // If no header or not Bearer token → continue filter chain
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            // Check if token is expired first
            if (jwtUtil.isTokenExpiredGracefully(token)) {
                System.out.println("JWT Filter - Token has expired");
                sendErrorResponse(response, "Token expired", "Your session has expired. Please login again.", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String email = jwtUtil.extractEmail(token);
            System.out.println("JWT Filter - Extracted email: " + email);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                System.out.println("JWT Filter - User authorities: " + userDetails.getAuthorities());

                // Validate token before authenticating
                if (jwtUtil.validateToken(token, userDetails)) {
                    
                    System.out.println("JWT Filter - Token validated successfully");

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("JWT Filter - Authentication set in SecurityContext");
                } else {
                    System.out.println("JWT Filter - Token validation failed");
                    sendErrorResponse(response, "Invalid token", "Authentication failed", HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            }
        } catch (ExpiredJwtException e) {
            System.err.println("JWT Filter - Token expired: " + e.getMessage());
            sendErrorResponse(response, "Token expired", "Your session has expired. Please login again.", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        } catch (SignatureException e) {
            System.err.println("JWT Filter - Invalid signature: " + e.getMessage());
            sendErrorResponse(response, "Invalid token", "Invalid token signature", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        } catch (MalformedJwtException e) {
            System.err.println("JWT Filter - Malformed token: " + e.getMessage());
            sendErrorResponse(response, "Invalid token", "Token format is invalid", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        } catch (Exception e) {
            System.err.println("JWT Filter - Unexpected error: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(response, "Authentication error", "An error occurred during authentication", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, String error, String message, int statusCode) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", error);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", String.valueOf(System.currentTimeMillis()));
        
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}