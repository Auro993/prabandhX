package com.prabandhx.prabandhx.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_project_id", columnList = "project_id"),
    @Index(name = "idx_entity_type", columnList = "entity_type"),
    @Index(name = "idx_action_type", columnList = "action_type"),
    @Index(name = "idx_timestamp", columnList = "timestamp")
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===== USER INFORMATION =====
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "user_name")
    private String userName;

    // ===== PROJECT INFORMATION =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "tasks", "collaborators"})
    private Project project;

    @Column(name = "project_name")
    private String projectName;

    // ===== ENTITY INFORMATION (WHAT WAS AFFECTED) =====
    @Column(name = "entity_type")
    private String entityType; // TASK, PROJECT, FILE, USER, COLLABORATOR, etc.

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "entity_name")
    private String entityName;

    // ===== ACTION INFORMATION =====
    @Column(nullable = false)
    private String action;

    @Column(name = "action_type")
    private String actionType; // CREATE, UPDATE, DELETE, UPLOAD, DOWNLOAD, LOGIN, LOGOUT, etc.

    @Column(length = 1000)
    private String details;

    // ===== TIMESTAMP =====
    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    // ===== REQUEST INFORMATION =====
    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    // ===== METADATA (JSON format for additional data) =====
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // Store as JSON string

    // ===== CONSTRUCTORS =====

    public ActivityLog() {
        this.timestamp = LocalDateTime.now();
    }

    public ActivityLog(Project project, String userEmail, String action, String details) {
        this.project = project;
        this.userEmail = userEmail;
        this.action = action;
        this.details = details;
        this.timestamp = LocalDateTime.now();
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

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
        if (project != null) {
            this.projectName = project.getName();
        }
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

    public Long getProjectId() {
        return project != null ? project.getId() : null;
    }

    /**
     * Check if this log is for a specific entity type
     */
    public boolean isForEntityType(String type) {
        return entityType != null && entityType.equalsIgnoreCase(type);
    }

    /**
     * Check if this log is for a specific action type
     */
    public boolean isForActionType(String type) {
        return actionType != null && actionType.equalsIgnoreCase(type);
    }

    /**
     * Format timestamp for display
     */
    public String getFormattedTimestamp() {
        return timestamp != null ? 
            timestamp.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")) : 
            "Unknown";
    }

    @Override
    public String toString() {
        return "ActivityLog{" +
                "id=" + id +
                ", userEmail='" + userEmail + '\'' +
                ", action='" + action + '\'' +
                ", actionType='" + actionType + '\'' +
                ", entityType='" + entityType + '\'' +
                ", projectId=" + getProjectId() +
                ", timestamp=" + timestamp +
                '}';
    }
}