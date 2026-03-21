package com.prabandhx.prabandhx.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Entity
@Table(name = "projects")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "projects", "users"})
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "organization", "projects"})
    private User manager;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Task> tasks;

    // Collaborators relationship
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "project"})
    private List<Collaborator> collaborators;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private String status; // ACTIVE, COMPLETED, ON_HOLD

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    // Guest access settings
    @Column(name = "allow_guest_access")
    private Boolean allowGuestAccess = true;

    @Column(name = "default_guest_permission")
    private String defaultGuestPermission = "VIEWER";
    
    // ===== NEW: GANTT CHART SETTINGS =====
    
    @Column(name = "gantt_settings", columnDefinition = "TEXT")
    private String ganttSettings; // JSON string for Gantt view settings
    
    @Column(name = "gantt_zoom_level")
    private String ganttZoomLevel = "WEEK"; // DAY, WEEK, MONTH, QUARTER
    
    @Column(name = "gantt_show_weekends")
    private Boolean ganttShowWeekends = false;
    
    @Column(name = "gantt_row_height")
    private Integer ganttRowHeight = 40; // pixels

    // ===== CONSTRUCTORS =====
    
    public Project() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = "ACTIVE";
        this.allowGuestAccess = true;
        this.defaultGuestPermission = "VIEWER";
        this.ganttZoomLevel = "WEEK";
        this.ganttShowWeekends = false;
        this.ganttRowHeight = 40;
        this.ganttSettings = "{}";
        this.tasks = new ArrayList<>();
        this.collaborators = new ArrayList<>();
    }

    public Project(String name, String description, Organization organization, User manager) {
        this.name = name;
        this.description = description;
        this.organization = organization;
        this.manager = manager;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = "ACTIVE";
        this.allowGuestAccess = true;
        this.defaultGuestPermission = "VIEWER";
        this.ganttZoomLevel = "WEEK";
        this.ganttShowWeekends = false;
        this.ganttRowHeight = 40;
        this.ganttSettings = "{}";
        this.tasks = new ArrayList<>();
        this.collaborators = new ArrayList<>();
    }

    // ===== GETTERS & SETTERS =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public User getManager() {
        return manager;
    }

    public void setManager(User manager) {
        this.manager = manager;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public List<Collaborator> getCollaborators() {
        return collaborators;
    }

    public void setCollaborators(List<Collaborator> collaborators) {
        this.collaborators = collaborators;
    }

    public Boolean getAllowGuestAccess() {
        return allowGuestAccess;
    }

    public void setAllowGuestAccess(Boolean allowGuestAccess) {
        this.allowGuestAccess = allowGuestAccess;
    }

    public String getDefaultGuestPermission() {
        return defaultGuestPermission;
    }

    public void setDefaultGuestPermission(String defaultGuestPermission) {
        this.defaultGuestPermission = defaultGuestPermission;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
    
    // ===== NEW GETTERS & SETTERS FOR GANTT =====
    
    public String getGanttSettings() {
        return ganttSettings;
    }

    public void setGanttSettings(String ganttSettings) {
        this.ganttSettings = ganttSettings;
    }

    public String getGanttZoomLevel() {
        return ganttZoomLevel;
    }

    public void setGanttZoomLevel(String ganttZoomLevel) {
        this.ganttZoomLevel = ganttZoomLevel;
    }

    public Boolean getGanttShowWeekends() {
        return ganttShowWeekends;
    }

    public void setGanttShowWeekends(Boolean ganttShowWeekends) {
        this.ganttShowWeekends = ganttShowWeekends;
    }

    public Integer getGanttRowHeight() {
        return ganttRowHeight;
    }

    public void setGanttRowHeight(Integer ganttRowHeight) {
        this.ganttRowHeight = ganttRowHeight;
    }

    // ===== HELPER METHODS =====
    
    public Long getOrganizationId() {
        return organization != null ? organization.getId() : null;
    }

    public String getOrganizationName() {
        return organization != null ? organization.getName() : null;
    }

    public Long getManagerId() {
        return manager != null ? manager.getId() : null;
    }

    public String getManagerName() {
        return manager != null ? manager.getName() : null;
    }

    public int getTaskCount() {
        if (tasks == null) {
            return 0;
        }
        return tasks.size();
    }
    
    /**
     * Get completed task count
     */
    public int getCompletedTaskCount() {
        if (tasks == null) {
            return 0;
        }
        return (int) tasks.stream()
                .filter(task -> task != null && "COMPLETED".equals(task.getStatus()))
                .count();
    }
    
    /**
     * Get overall project progress percentage
     */
    public int getOverallProgress() {
        if (tasks == null || tasks.isEmpty()) {
            return 0;
        }
        
        int totalProgress = 0;
        int validTasks = 0;
        
        for (Task task : tasks) {
            if (task != null) {
                Integer progress = task.getProgress();
                if (progress != null) {
                    totalProgress += progress;
                    validTasks++;
                }
            }
        }
        
        if (validTasks == 0) {
            return 0;
        }
        
        return totalProgress / validTasks;
    }
    
    /**
     * Get tasks with no start date (need scheduling)
     */
    public List<Task> getUnscheduledTasks() {
        if (tasks == null) {
            return new ArrayList<>();
        }
        
        List<Task> unscheduled = new ArrayList<>();
        for (Task task : tasks) {
            if (task != null && task.getStartDate() == null && !task.isCompleted()) {
                unscheduled.add(task);
            }
        }
        return unscheduled;
    }

    public int getActiveCollaboratorCount() {
        if (collaborators == null) {
            return 0;
        }
        int count = 0;
        for (Collaborator c : collaborators) {
            if (c != null && c.getIsActive() != null && c.getIsActive() && 
                c.getIsAccepted() != null && c.getIsAccepted()) {
                count++;
            }
        }
        return count;
    }

    public int getPendingInvitationCount() {
        if (collaborators == null) {
            return 0;
        }
        int count = 0;
        for (Collaborator c : collaborators) {
            if (c != null && c.getIsActive() != null && c.getIsActive() && 
                c.getIsAccepted() != null && !c.getIsAccepted()) {
                count++;
            }
        }
        return count;
    }

    public boolean hasCollaborator(String email) {
        if (collaborators == null || email == null) {
            return false;
        }
        for (Collaborator c : collaborators) {
            if (c != null && email.equalsIgnoreCase(c.getEmail()) && 
                c.getIsActive() != null && c.getIsActive() && 
                c.getIsAccepted() != null && c.getIsAccepted()) {
                return true;
            }
        }
        return false;
    }

    public Collaborator getCollaboratorByEmail(String email) {
        if (collaborators == null || email == null) {
            return null;
        }
        for (Collaborator c : collaborators) {
            if (c != null && email.equalsIgnoreCase(c.getEmail()) && 
                c.getIsActive() != null && c.getIsActive()) {
                return c;
            }
        }
        return null;
    }

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    public boolean isOnHold() {
        return "ON_HOLD".equals(status);
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", organizationId=" + getOrganizationId() +
                ", managerId=" + getManagerId() +
                ", overallProgress=" + getOverallProgress() + "%" +
                ", activeCollaborators=" + getActiveCollaboratorCount() +
                ", pendingInvitations=" + getPendingInvitationCount() +
                '}';
    }
}