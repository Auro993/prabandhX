package com.prabandhx.prabandhx.controller;

import com.prabandhx.prabandhx.dto.ActivityLogDTO;
import com.prabandhx.prabandhx.service.ActivityLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/activity")
@CrossOrigin(origins = "*")
public class ActivityLogController {

    @Autowired
    private ActivityLogService activityLogService;

    /**
     * Get current authenticated user email
     */
    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) auth.getPrincipal()).getUsername();
        }
        return null;
    }

    // ===============================
    // PROJECT LOGS
    // ===============================

    /**
     * Get logs for a specific project (Manager/Admin)
     */
    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<ActivityLogDTO>> getProjectLogs(@PathVariable Long projectId) {
        return ResponseEntity.ok(activityLogService.getProjectLogs(projectId));
    }

    /**
     * Get recent logs for a project with limit
     */
    @GetMapping("/project/{projectId}/recent")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<ActivityLogDTO>> getRecentProjectLogs(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(activityLogService.getRecentProjectLogs(projectId, limit));
    }

    // ===============================
    // USER LOGS
    // ===============================

    /**
     * Get logs for current user
     */
    @GetMapping("/my-activity")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ActivityLogDTO>> getMyActivity() {
        String email = getCurrentUserEmail();
        // You'd need to get user ID from email
        return ResponseEntity.ok(activityLogService.getUserLogs(1L)); // Replace with actual user ID lookup
    }

    /**
     * Get logs for a specific user (Admin only)
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ActivityLogDTO>> getUserLogs(@PathVariable Long userId) {
        return ResponseEntity.ok(activityLogService.getUserLogs(userId));
    }

    // ===============================
    // ENTITY LOGS
    // ===============================

    /**
     * Get logs for a specific entity (task, file, etc.)
     */
    @GetMapping("/entity")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ActivityLogDTO>> getEntityLogs(
            @RequestParam String entityType,
            @RequestParam Long entityId) {
        return ResponseEntity.ok(activityLogService.getEntityLogs(entityType, entityId));
    }

    // ===============================
    // ACTION TYPE LOGS
    // ===============================

    /**
     * Get logs by action type
     */
    @GetMapping("/action/{actionType}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ActivityLogDTO>> getLogsByActionType(@PathVariable String actionType) {
        return ResponseEntity.ok(activityLogService.getLogsByActionType(actionType));
    }

    // ===============================
    // DATE RANGE LOGS
    // ===============================

    /**
     * Get logs within date range
     */
    @GetMapping("/range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ActivityLogDTO>> getLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(activityLogService.getLogsByDateRange(start, end));
    }

    /**
     * Get today's logs
     */
    @GetMapping("/today")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ActivityLogDTO>> getTodayLogs() {
        return ResponseEntity.ok(activityLogService.getTodayLogs());
    }

    // ===============================
    // RECENT LOGS (ADMIN)
    // ===============================

    /**
     * Get recent logs with pagination (Admin only)
     */
    @GetMapping("/recent")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ActivityLogDTO>> getRecentLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(activityLogService.getRecentLogs(page, size));
    }

    /**
     * Get recent logs with limit (Admin only)
     */
    @GetMapping("/recent/limit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ActivityLogDTO>> getRecentLogsWithLimit(
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(activityLogService.getRecentLogs(limit));
    }

    // ===============================
    // SEARCH
    // ===============================

    /**
     * Search logs by keyword (Admin only)
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ActivityLogDTO>> searchLogs(
            @RequestParam String keyword) {
        return ResponseEntity.ok(activityLogService.searchLogs(keyword));
    }

    /**
     * Search logs by project and keyword (Manager/Admin)
     */
    @GetMapping("/search/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<ActivityLogDTO>> searchLogsByProject(
            @PathVariable Long projectId,
            @RequestParam String keyword) {
        return ResponseEntity.ok(activityLogService.searchLogsByProject(projectId, keyword));
    }

    // ===============================
    // STATISTICS
    // ===============================

    /**
     * Get activity statistics (Admin only)
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getActivityStats() {
        return ResponseEntity.ok(activityLogService.getActivityStats());
    }

    /**
     * Get activity statistics for a project (Manager/Admin)
     */
    @GetMapping("/stats/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> getProjectActivityStats(@PathVariable Long projectId) {
        return ResponseEntity.ok(activityLogService.getProjectActivityStats(projectId));
    }

    // ===============================
    // MANUAL LOGGING (for testing)
    // ===============================

    /**
     * Manually log an activity (for testing)
     */
    @PostMapping("/log")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ActivityLogDTO> manualLog(
            @RequestParam String action,
            @RequestParam String details) {
        String email = getCurrentUserEmail();
        ActivityLogDTO log = activityLogService.log(email, action, details);
        return ResponseEntity.ok(log);
    }

    // ===============================
    // CLEANUP (ADMIN ONLY)
    // ===============================

    /**
     * Clean up old logs (Admin only)
     */
    @DeleteMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> cleanupOldLogs(
            @RequestParam(defaultValue = "30") int daysOld) {
        int deleted = activityLogService.cleanupOldLogs(daysOld);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Deleted " + deleted + " old log entries");
        response.put("deletedCount", deleted);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Activity Log Service is running");
        return ResponseEntity.ok(response);
    }
}