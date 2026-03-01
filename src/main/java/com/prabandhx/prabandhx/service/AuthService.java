package com.prabandhx.prabandhx.service;

import com.prabandhx.prabandhx.dto.AuthRequest;
import com.prabandhx.prabandhx.dto.AuthResponse;
import com.prabandhx.prabandhx.dto.RegisterRequest;
import com.prabandhx.prabandhx.entity.Organization;
import com.prabandhx.prabandhx.entity.User;
import com.prabandhx.prabandhx.repository.OrganizationRepository;
import com.prabandhx.prabandhx.repository.UserRepository;
import com.prabandhx.prabandhx.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

    // ============================
    // REGISTER USER
    // ============================
    public void register(RegisterRequest request) {

        // Check email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        // Find organization
        Organization org = organizationRepository
                .findById(request.getOrganizationId())
                .orElseThrow(() ->
                        new RuntimeException("Organization not found"));

        // Create user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(
                encoder.encode(request.getPassword())
        );

        // Always store role with ROLE_ prefix
        user.setRole("ROLE_" + request.getRole());

        user.setOrganization(org);

        userRepository.save(user);
    }

    // ============================
    // LOGIN USER
    // ============================
    public AuthResponse login(AuthRequest request) {

        // Find user
        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // Check password
        if (!encoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new RuntimeException("Invalid credentials");
        }

        // Generate JWT with ROLE
        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole()
        );

        return new AuthResponse(token);
    }
}