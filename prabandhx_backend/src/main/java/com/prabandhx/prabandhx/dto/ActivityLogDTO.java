package com.prabandhx.prabandhx.dto;

import java.time.LocalDateTime;

public class ActivityLogDTO {
    private Long id;
    private Long userId;
    private String userEmail;
    private String userName;
    
    private Long projectId;
    private String projectName;
    
    private String entityType;
    private Long entityId;
    private String entityName;
    
    private String action;
    private String actionType;
    private String details;
    
    private LocalDateTime timestamp;
    private String formattedTimestamp;
    
    private String ipAddress;
    private String userAgent;
    
    private String metadata;
    
    // ===== CONSTRUCTORS =====
    
    public ActivityLogDTO() {}

    public ActivityLogDTO(Long id, String userEmail, String userName, String action, 
                          String details, LocalDateTime timestamp, Long projectId, String projectName) {
        this.id = id;
        this.userEmail = userEmail;
        this.userName = userName;
        this.action = action;
        this.details = details;
        this.timestamp = timestamp;
        this.projectId = projectId;
        this.projectName = projectName;
    }

    // ===== GETTERS & SETTERS =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
        if (timestamp != null) {
            this.formattedTimestamp = timestamp.format(
                java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
            );
        }
    }

    public String getFormattedTimestamp() {
        return formattedTimestamp;
    }

    public void setFormattedTimestamp(String formattedTimestamp) {
        this.formattedTimestamp = formattedTimestamp;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    // ===== HELPER METHODS =====

    /**
     * Get a display-friendly action icon
     */
    public String getActionIcon() {
        if (actionType == null) return "📝";
        
        switch (actionType.toUpperCase()) {
            case "CREATE_TASK":
            case "CREATE_PROJECT":
            case "CREATE":
                return "✅";
            case "UPDATE_TASK":
            case "UPDATE_PROJECT":
            case "UPDATE":
                return "✏️";
            case "DELETE_TASK":
            case "DELETE_PROJECT":
            case "DELETE":
                return "🗑️";
            case "UPLOAD_FILE":
                return "📤";
            case "DOWNLOAD_FILE":
                return "📥";
            case "LOGIN":
                return "🔐";
            case "LOGOUT":
                return "🚪";
            case "INVITE_COLLABORATOR":
                return "🤝";
            case "ACCEPT_INVITE":
                return "✅";
            case "REMOVE_COLLABORATOR":
                return "👋";
            default:
                return "📝";
        }
    }

    /**
     * Get color class based on action type
     */
    public String getColorClass() {
        if (actionType == null) return "default";
        
        if (actionType.contains("CREATE")) return "create";
        if (actionType.contains("UPDATE")) return "update";
        if (actionType.contains("DELETE")) return "delete";
        if (actionType.contains("UPLOAD")) return "upload";
        if (actionType.contains("DOWNLOAD")) return "download";
        if (actionType.contains("LOGIN")) return "login";
        if (actionType.contains("INVITE")) return "invite";
        if (actionType.contains("ACCEPT")) return "accept";
        
        return "default";
    }

    @Override
    public String toString() {
        return "ActivityLogDTO{" +
                "id=" + id +
                ", userEmail='" + userEmail + '\'' +
                ", action='" + action + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}