package com.prabandhx.prabandhx.controller;

import com.prabandhx.prabandhx.dto.UserDTO;
import com.prabandhx.prabandhx.service.LeaderboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leaderboard")
@CrossOrigin(origins = "*")
public class LeaderboardController {
    
    @Autowired
    private LeaderboardService leaderboardService;
    
    /**
     * Get top performers by points
     */
    @GetMapping("/points")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserDTO>> getTopByPoints(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(leaderboardService.getTopPerformers(limit));
    }
    
    /**
     * Get top performers by streak
     */
    @GetMapping("/streaks")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserDTO>> getTopByStreaks(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(leaderboardService.getTopStreakers(limit));
    }
    
    /**
     * Get user's rank
     */
    @GetMapping("/rank/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getUserRank(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        response.put("pointsRank", leaderboardService.getUserRankByPoints(userId));
        response.put("streakRank", leaderboardService.getUserRankByStreak(userId));
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get combined leaderboard (both points and streaks)
     */
    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getAllLeaderboards(
            @RequestParam(defaultValue = "5") int limit) {
        Map<String, Object> response = new HashMap<>();
        response.put("topByPoints", leaderboardService.getTopPerformers(limit));
        response.put("topByStreaks", leaderboardService.getTopStreakers(limit));
        return ResponseEntity.ok(response);
    }
}