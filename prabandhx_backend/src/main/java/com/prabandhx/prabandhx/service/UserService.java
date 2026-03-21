package com.prabandhx.prabandhx.service;

import com.prabandhx.prabandhx.entity.User;
import com.prabandhx.prabandhx.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ActivityLogService activityLogService;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found with id: " + id));
    }
    
    @Transactional
    public User updateUserRole(Long id, String newRole) {
        User user = getUserById(id);
        String oldRole = user.getRole();
        
        user.setRole(newRole);
        User updatedUser = userRepository.save(user);
        
        // ✅ LOG ACTIVITY: User Role Updated
        activityLogService.logActivity(
            id,
            "UPDATE_USER_ROLE",
            "Updated user role",
            "USER",
            id,
            user.getName(),
            null,
            "User role changed from " + oldRole + " to " + newRole,
            null
        );
        
        return updatedUser;
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        String userName = user.getName();
        String userEmail = user.getEmail();
        
        userRepository.deleteById(id);
        
        // ✅ LOG ACTIVITY: User Deleted
        activityLogService.logActivity(
            null,
            "DELETE_USER",
            "Deleted user",
            "USER",
            id,
            userName,
            null,
            "User '" + userName + "' (" + userEmail + ") was deleted",
            null
        );
    }
}