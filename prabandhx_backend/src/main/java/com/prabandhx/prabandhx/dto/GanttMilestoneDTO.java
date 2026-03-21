package com.prabandhx.prabandhx.dto;

import java.time.LocalDateTime;

public class GanttMilestoneDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private Boolean isCompleted;
    private String color;
    private String icon;
    private Long projectId;
    private String projectName;

    public GanttMilestoneDTO() {}

    public GanttMilestoneDTO(Long id, String title, String description, LocalDateTime dueDate,
                             Boolean isCompleted, String color, String icon, Long projectId, String projectName) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.isCompleted = isCompleted;
        this.color = color;
        this.icon = icon;
        this.projectId = projectId;
        this.projectName = projectName;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public Boolean getIsCompleted() { return isCompleted; }
    public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
}