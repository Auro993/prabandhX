package com.prabandhx.prabandhx.security;

import com.prabandhx.prabandhx.entity.User;
import com.prabandhx.prabandhx.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email: " + email));

        System.out.println("✅ User found: " + user.getEmail() + " with role: " + user.getRole());
        return new CustomUserDetails(user);
    }
}