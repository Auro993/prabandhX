package com.prabandhx.prabandhx.service;

import com.prabandhx.prabandhx.dto.AuthRequest;
import com.prabandhx.prabandhx.dto.AuthResponse;
import com.prabandhx.prabandhx.dto.RegisterRequest;
import com.prabandhx.prabandhx.entity.Organization;
import com.prabandhx.prabandhx.entity.User;
import com.prabandhx.prabandhx.repository.OrganizationRepository;
import com.prabandhx.prabandhx.repository.UserRepository;
import com.prabandhx.prabandhx.security.JwtUtil;
import com.prabandhx.prabandhx.config.AdminConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    
    @Autowired
    private AdminConfig adminConfig;
    
    @Autowired
    private ActivityLogService activityLogService;

    // =========================
    // REGISTER USER
    // =========================
    @Transactional
    public void register(RegisterRequest request) {

        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        Organization organization;
        String role = request.getRole().trim().toUpperCase();

        // =========================
        // ADMIN REGISTRATION - WITH EMAIL RESTRICTION
        // =========================
        if ("ADMIN".equals(role)) {
            
            // 🔥 NEW: Check if email is allowed to be admin
            if (!adminConfig.isAllowedAdmin(request.getEmail())) {
                throw new RuntimeException("This email is not authorized for admin registration");
            }

            if (request.getOrganizationName() == null ||
                    request.getOrganizationName().isEmpty()) {

                throw new RuntimeException("Organization name required for Admin");
            }

            Organization newOrg = new Organization();
            newOrg.setName(request.getOrganizationName());

            // Save and assign
            organization = organizationRepository.save(newOrg);
        }

        // =========================
        // MANAGER / USER REGISTRATION
        // =========================
        else if ("MANAGER".equals(role) || "USER".equals(role)) {

            if (request.getOrganizationId() == null) {
                throw new RuntimeException("Organization ID required");
            }

            organization = organizationRepository
                    .findById(request.getOrganizationId())
                    .orElseThrow(() ->
                            new RuntimeException("Organization not found"));
        }

        else {
            throw new RuntimeException("Invalid role");
        }

        // =========================
        // CREATE USER
        // =========================
        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        // Encrypt password
        user.setPassword(
                encoder.encode(request.getPassword())
        );

        // Store role with ROLE_ prefix
        user.setRole("ROLE_" + role);

        // Assign organization
        user.setOrganization(organization);

        userRepository.save(user);
        
        // ✅ LOG ACTIVITY: User Registered
        activityLogService.logActivity(
            user.getId(),
            "REGISTER_USER",
            "User registered",
            "USER",
            user.getId(),
            user.getName(),
            null,
            "New user registered: " + user.getName() + " (" + user.getEmail() + ") as " + role,
            null
        );
    }

    // =========================
    // LOGIN USER
    // =========================
    public AuthResponse login(AuthRequest request) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (!encoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole()
        );
        
        // ✅ LOG ACTIVITY: User Logged In
        activityLogService.logActivity(
            user.getId(),
            "LOGIN",
            "User logged in",
            "USER",
            user.getId(),
            user.getName(),
            null,
            "User logged in from IP: " + getClientIp(),
            null
        );

        return new AuthResponse(token);
    }
    
    // =========================
    // LOGOUT (optional tracking)
    // =========================
    public void logout(Long userId) {
        // You can track logout if you want
        activityLogService.logActivity(
            userId,
            "LOGOUT",
            "User logged out",
            "USER",
            userId,
            null,
            null,
            "User logged out",
            null
        );
    }
    
    private String getClientIp() {
        // This would need HttpServletRequest injected
        // For now, return a placeholder
        return "unknown";
    }
}