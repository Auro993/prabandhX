package com.prabandhx.prabandhx.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tasks")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String status; // TODO, IN_PROGRESS, COMPLETED

    private String priority; // HIGH, MEDIUM, LOW

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "tasks", "organization", "manager"})
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "organization", "projects"})
    private User assignedTo;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    private Integer storyPoints; // For agile/scrum estimation

    // ===== NEW: GANTT CHART FIELDS =====
    
    @Column(name = "start_date")
    private LocalDateTime startDate;
    
    @Column(name = "end_date")
    private LocalDateTime endDate;
    
    @Column(name = "progress")
    private Integer progress = 0; // 0-100 percentage
    
    @Column(name = "is_milestone")
    private Boolean isMilestone = false;
    
    @Column(name = "duration_days")
    private Integer durationDays;
    
    @Column(name = "gantt_color")
    private String ganttColor; // Custom color for Gantt bar

    // ===== CONSTRUCTORS =====
    
    public Task() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = "TODO";
        this.priority = "MEDIUM";
        this.progress = 0;
        this.isMilestone = false;
    }

    public Task(String title, String description, Project project, User assignedTo) {
        this.title = title;
        this.description = description;
        this.project = project;
        this.assignedTo = assignedTo;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = "TODO";
        this.priority = "MEDIUM";
        this.progress = 0;
        this.isMilestone = false;
    }

    // ===== GETTERS & SETTERS =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        // Auto-set completedAt when status changes to COMPLETED
        if ("COMPLETED".equals(status) && completedAt == null) {
            this.completedAt = LocalDateTime.now();
            this.progress = 100;
        }
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Integer getStoryPoints() {
        return storyPoints;
    }

    public void setStoryPoints(Integer storyPoints) {
        this.storyPoints = storyPoints;
    }

    // ===== NEW GETTERS & SETTERS FOR GANTT =====
    
    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
        // Auto-calculate duration if endDate exists
        if (startDate != null && endDate != null) {
            long days = java.time.Duration.between(startDate, endDate).toDays();
            this.durationDays = (int) Math.max(days, 1);
        }
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
        // Auto-calculate duration if startDate exists
        if (startDate != null && endDate != null) {
            long days = java.time.Duration.between(startDate, endDate).toDays();
            this.durationDays = (int) Math.max(days, 1);
        }
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        if (progress != null && progress >= 0 && progress <= 100) {
            this.progress = progress;
            // Auto-complete if progress reaches 100
            if (progress == 100 && !"COMPLETED".equals(status)) {
                this.status = "COMPLETED";
                this.completedAt = LocalDateTime.now();
            }
        }
    }

    public Boolean getIsMilestone() {
        return isMilestone;
    }

    public void setIsMilestone(Boolean isMilestone) {
        this.isMilestone = isMilestone;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }

    public String getGanttColor() {
        return ganttColor;
    }

    public void setGanttColor(String ganttColor) {
        this.ganttColor = ganttColor;
    }

    // ===== HELPER METHODS =====
    
    /**
     * Get project ID without triggering lazy loading
     */
    public Long getProjectId() {
        if (project == null) {
            return null;
        }
        return project.getId();
    }

    /**
     * Get project name without triggering lazy loading
     */
    public String getProjectName() {
        if (project == null) {
            return null;
        }
        return project.getName();
    }

    /**
     * Get assigned user ID without triggering lazy loading
     */
    public Long getAssignedToId() {
        if (assignedTo == null) {
            return null;
        }
        return assignedTo.getId();
    }

    /**
     * Get assigned user name without triggering lazy loading
     */
    public String getAssignedToName() {
        if (assignedTo == null) {
            return null;
        }
        return assignedTo.getName();
    }

    /**
     * Check if task is overdue
     */
    public boolean isOverdue() {
        if (dueDate == null) {
            return false;
        }
        return dueDate.isBefore(LocalDateTime.now()) && !"COMPLETED".equals(status);
    }

    /**
     * Check if task is completed
     */
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    /**
     * Check if task is in progress
     */
    public boolean isInProgress() {
        return "IN_PROGRESS".equals(status);
    }

    /**
     * Get time spent on task (if completed)
     */
    public Long getTimeSpentInHours() {
        if (completedAt != null && createdAt != null) {
            return java.time.Duration.between(createdAt, completedAt).toHours();
        }
        return null;
    }
    
    /**
     * Get progress as percentage string
     */
    public String getProgressPercentage() {
        if (progress == null) {
            return "0%";
        }
        return progress + "%";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        // Update duration if dates changed
        if (startDate != null && endDate != null) {
            long days = java.time.Duration.between(startDate, endDate).toDays();
            this.durationDays = (int) Math.max(days, 1);
        }
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", status='" + status + '\'' +
                ", priority='" + priority + '\'' +
                ", progress=" + progress +
                ", isMilestone=" + isMilestone +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", projectId=" + getProjectId() +
                ", assignedToId=" + getAssignedToId() +
                ", dueDate=" + dueDate +
                '}';
    }
}