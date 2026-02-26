package com.prabandhx.prabandhx.service;

import com.prabandhx.prabandhx.dto.*;
import com.prabandhx.prabandhx.entity.*;
import com.prabandhx.prabandhx.repository.*;
import com.prabandhx.prabandhx.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BCryptPasswordEncoder encoder;

    public void register(RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        Organization org =
                organizationRepository.findById(
                        request.getOrganizationId())
                        .orElseThrow(() ->
                                new RuntimeException("Organization not found"));

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(encoder.encode(request.getPassword()));

        // Always store role with ROLE_ prefix
        user.setRole("ROLE_" + request.getRole());

        user.setOrganization(org);

        userRepository.save(user);
    }

    public AuthResponse login(AuthRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (!encoder.matches(request.getPassword(),
                user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        return new AuthResponse(token);
    }
}