package com.prabandhx.prabandhx.service;

import com.prabandhx.prabandhx.dto.UserDTO;
import com.prabandhx.prabandhx.entity.User;
import com.prabandhx.prabandhx.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaderboardService {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Get top performers by total points
     */
    public List<UserDTO> getTopPerformers(int limit) {
        return userRepository.findTopPerformers(limit)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get top streakers by current streak
     */
    public List<UserDTO> getTopStreakers(int limit) {
        return userRepository.findTopStreakers(limit)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get user's rank by points
     */
    public int getUserRankByPoints(Long userId) {
        List<User> allUsers = userRepository.findAll();
        List<User> sortedUsers = allUsers.stream()
                .sorted((a, b) -> b.getTotalPoints().compareTo(a.getTotalPoints()))
                .collect(Collectors.toList());
        
        for (int i = 0; i < sortedUsers.size(); i++) {
            if (sortedUsers.get(i).getId().equals(userId)) {
                return i + 1;
            }
        }
        return -1;
    }
    
    /**
     * Get user's rank by streak
     */
    public int getUserRankByStreak(Long userId) {
        List<User> allUsers = userRepository.findAll();
        List<User> sortedUsers = allUsers.stream()
                .sorted((a, b) -> b.getCurrentStreak().compareTo(a.getCurrentStreak()))
                .collect(Collectors.toList());
        
        for (int i = 0; i < sortedUsers.size(); i++) {
            if (sortedUsers.get(i).getId().equals(userId)) {
                return i + 1;
            }
        }
        return -1;
    }
    
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setOrganizationId(user.getOrganizationId());
        dto.setTotalPoints(user.getTotalPoints());
        dto.setCurrentStreak(user.getCurrentStreak());
        dto.setLongestStreak(user.getLongestStreak());
        dto.setLastCompletedDate(user.getLastCompletedDate());
        return dto;
    }
}