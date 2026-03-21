package com.prabandhx.prabandhx.service;

import com.prabandhx.prabandhx.dto.ActivityLogDTO;
import com.prabandhx.prabandhx.entity.ActivityLog;
import com.prabandhx.prabandhx.entity.Project;
import com.prabandhx.prabandhx.entity.User;
import com.prabandhx.prabandhx.repository.ActivityLogRepository;
import com.prabandhx.prabandhx.repository.ProjectRepository;
import com.prabandhx.prabandhx.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ActivityLogService {

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired(required = false)
    private HttpServletRequest request;

    @Autowired
    private ObjectMapper objectMapper;

    // ===============================
    // LOGGING METHODS
    // ===============================

    /**
     * Log an activity with full details
     */
    @Transactional
    public ActivityLogDTO logActivity(
            Long userId,
            String actionType,
            String action,
            String entityType,
            Long entityId,
            String entityName,
            Long projectId,
            String details,
            Map<String, Object> metadata
    ) {
        ActivityLog log = new ActivityLog();
        
        // Set user info
        if (userId != null) {
            userRepository.findById(userId).ifPresent(user -> {
                log.setUserId(user.getId());
                log.setUserEmail(user.getEmail());
                log.setUserName(user.getName());
            });
        }
        
        // Set project info
        if (projectId != null) {
            projectRepository.findById(projectId).ifPresent(project -> {
                log.setProject(project);
                log.setProjectName(project.getName());
            });
        }
        
        log.setActionType(actionType);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setEntityName(entityName);
        log.setDetails(details);
        log.setTimestamp(LocalDateTime.now());
        
        // Set request info
        if (request != null) {
            log.setIpAddress(getClientIp(request));
            log.setUserAgent(request.getHeader("User-Agent"));
        }
        
        // Set metadata as JSON
        if (metadata != null && !metadata.isEmpty()) {
            try {
                log.setMetadata(objectMapper.writeValueAsString(metadata));
            } catch (Exception e) {
                log.setMetadata("{}");
            }
        }
        
        ActivityLog savedLog = activityLogRepository.save(log);
        return convertToDTO(savedLog);
    }

    /**
     * Simplified log method (for quick logging)
     */
    @Transactional
    public ActivityLogDTO log(String userEmail, String action, String details) {
        return userRepository.findByEmail(userEmail)
                .map(user -> logActivity(
                    user.getId(),
                    action.toUpperCase().replace(" ", "_"),
                    action,
                    null,
                    null,
                    null,
                    null,
                    details,
                    null
                ))
                .orElse(null);
    }

    /**
     * Log task activity
     */
    @Transactional
    public ActivityLogDTO logTaskActivity(Long userId, Long taskId, String taskTitle, 
                                          Long projectId, String actionType, String details) {
        return logActivity(
            userId,
            actionType,
            getActionDisplayName(actionType, "Task"),
            "TASK",
            taskId,
            taskTitle,
            projectId,
            details,
            null
        );
    }

    /**
     * Log file activity
     */
    @Transactional
    public ActivityLogDTO logFileActivity(Long userId, Long fileId, String fileName, 
                                          Long projectId, String actionType, String details, 
                                          Map<String, Object> metadata) {
        return logActivity(
            userId,
            actionType,
            getActionDisplayName(actionType, "File"),
            "FILE",
            fileId,
            fileName,
            projectId,
            details,
            metadata
        );
    }

    /**
     * Log project activity
     */
    @Transactional
    public ActivityLogDTO logProjectActivity(Long userId, Long projectId, String projectName, 
                                             String actionType, String details) {
        return logActivity(
            userId,
            actionType,
            getActionDisplayName(actionType, "Project"),
            "PROJECT",
            projectId,
            projectName,
            projectId,
            details,
            null
        );
    }

    /**
     * Log user activity (login, logout, role change)
     */
    @Transactional
    public ActivityLogDTO logUserActivity(Long userId, String userEmail, String userName,
                                          String actionType, String details) {
        return logActivity(
            userId,
            actionType,
            getActionDisplayName(actionType, "User"),
            "USER",
            userId,
            userName,
            null,
            details,
            null
        );
    }

    /**
     * Log collaborator activity
     */
    @Transactional
    public ActivityLogDTO logCollaboratorActivity(Long userId, String collaboratorEmail,
                                                  Long projectId, String projectName,
                                                  String actionType, String details) {
        return logActivity(
            userId,
            actionType,
            getActionDisplayName(actionType, "Collaborator"),
            "COLLABORATOR",
            null,
            collaboratorEmail,
            projectId,
            details,
            null
        );
    }

    // ===============================
    // GET LOGS METHODS
    // ===============================

    /**
     * Get logs for a specific project
     */
    public List<ActivityLogDTO> getProjectLogs(Long projectId) {
        return activityLogRepository.findByProjectIdOrderByTimestampDesc(projectId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get recent logs for a project with limit - FIXED
     */
    public List<ActivityLogDTO> getRecentProjectLogs(Long projectId, int limit) {
        return activityLogRepository.findByProjectIdOrderByTimestampDesc(projectId)
                .stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get logs for a specific user
     */
    public List<ActivityLogDTO> getUserLogs(Long userId) {
        return activityLogRepository.findByUserIdOrderByTimestampDesc(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get recent logs for a user with limit - FIXED
     */
    public List<ActivityLogDTO> getRecentUserLogs(Long userId, int limit) {
        return activityLogRepository.findByUserIdOrderByTimestampDesc(userId)
                .stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get logs for a specific entity
     */
    public List<ActivityLogDTO> getEntityLogs(String entityType, Long entityId) {
        return activityLogRepository.findByEntity(entityType, entityId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get logs by action type
     */
    public List<ActivityLogDTO> getLogsByActionType(String actionType) {
        return activityLogRepository.findByActionType(actionType)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get logs within date range
     */
    public List<ActivityLogDTO> getLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        return activityLogRepository.findByDateRange(start, end)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get today's logs
     */
    public List<ActivityLogDTO> getTodayLogs() {
        return activityLogRepository.findTodayLogs()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get recent logs (Admin only) with pagination
     */
    public Page<ActivityLogDTO> getRecentLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return activityLogRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get recent logs with limit (without pagination) - FIXED
     */
    public List<ActivityLogDTO> getRecentLogs(int limit) {
        return activityLogRepository.findAllOrderByTimestampDesc()
                .stream()
                .limit(limit)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search logs by keyword
     */
    public List<ActivityLogDTO> searchLogs(String keyword) {
        return activityLogRepository.searchLogs(keyword)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search logs by project and keyword
     */
    public List<ActivityLogDTO> searchLogsByProject(Long projectId, String keyword) {
        return activityLogRepository.searchLogsByProject(projectId, keyword)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ===============================
    // RECENT ACTIVITY LOGS - FIXED
    // ===============================

    /**
     * Get recent activity logs for a project
     */
    public List<ActivityLogDTO> getRecentActivityLogs(Long projectId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        
        List<ActivityLog> logs = activityLogRepository.findRecentActivityByProject(projectId, since);
        return logs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ===============================
    // STATISTICS METHODS
    // ===============================

    /**
     * Get activity statistics for dashboard
     */
    public Map<String, Object> getActivityStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("today", activityLogRepository.getTodayActivityCount());
        
        List<Object[]> typeStats = activityLogRepository.getActivityTypeStats();
        Map<String, Long> typeMap = new HashMap<>();
        for (Object[] row : typeStats) {
            typeMap.put((String) row[0], (Long) row[1]);
        }
        stats.put("byType", typeMap);
        
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<Object[]> timeline = activityLogRepository.getActivityTimeline(weekAgo);
        stats.put("timeline", timeline);
        
        return stats;
    }

    /**
     * Get activity statistics for a specific project
     */
    public Map<String, Object> getProjectActivityStats(Long projectId) {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("today", activityLogRepository.getTodayActivityCountByProject(projectId));
        
        List<Object[]> typeStats = activityLogRepository.getActivityTypeStatsByProject(projectId);
        Map<String, Long> typeMap = new HashMap<>();
        for (Object[] row : typeStats) {
            typeMap.put((String) row[0], (Long) row[1]);
        }
        stats.put("byType", typeMap);
        
        return stats;
    }

    // ===============================
    // CLEANUP METHODS - FIXED
    // ===============================

    /**
     * Delete logs older than specified days (Admin only)
     */
    @Transactional
    public int cleanupOldLogs(int daysOld) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysOld);
        
        int deletedCount = activityLogRepository.deleteOlderThan(cutoff);
        
        System.out.println("🧹 Cleaned up " + deletedCount + " log entries older than " + daysOld + " days");
        
        return deletedCount;
    }

    /**
     * Delete all logs for a project (when project is deleted)
     */
    @Transactional
    public int deleteProjectLogs(Long projectId) {
        return activityLogRepository.deleteByProjectId(projectId);
    }

    // ===============================
    // HELPER METHODS
    // ===============================

    /**
     * Convert entity to DTO
     */
    private ActivityLogDTO convertToDTO(ActivityLog log) {
        ActivityLogDTO dto = new ActivityLogDTO();
        
        dto.setId(log.getId());
        dto.setUserId(log.getUserId());
        dto.setUserEmail(log.getUserEmail());
        dto.setUserName(log.getUserName());
        
        dto.setProjectId(log.getProjectId());
        dto.setProjectName(log.getProjectName());
        
        dto.setEntityType(log.getEntityType());
        dto.setEntityId(log.getEntityId());
        dto.setEntityName(log.getEntityName());
        
        dto.setAction(log.getAction());
        dto.setActionType(log.getActionType());
        dto.setDetails(log.getDetails());
        
        dto.setTimestamp(log.getTimestamp());
        
        // Format timestamp for display
        if (log.getTimestamp() != null) {
            dto.setFormattedTimestamp(log.getTimestamp().format(
                DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
            ));
        }
        
        dto.setIpAddress(log.getIpAddress());
        dto.setUserAgent(log.getUserAgent());
        dto.setMetadata(log.getMetadata());
        
        return dto;
    }

    /**
     * Get user-friendly action display name
     */
    private String getActionDisplayName(String actionType, String entity) {
        if (actionType == null) return "Performed action";
        
        switch (actionType.toUpperCase()) {
            case "CREATE":
                return "Created " + entity;
            case "UPDATE":
                return "Updated " + entity;
            case "DELETE":
                return "Deleted " + entity;
            case "UPLOAD":
                return "Uploaded " + entity;
            case "DOWNLOAD":
                return "Downloaded " + entity;
            case "LOGIN":
                return "Logged in";
            case "LOGOUT":
                return "Logged out";
            case "INVITE":
                return "Invited " + entity;
            case "ACCEPT":
                return "Accepted invitation";
            case "REMOVE":
                return "Removed " + entity;
            default:
                return actionType.replace("_", " ") + " " + entity;
        }
    }

    /**
     * Get client IP address from request
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}