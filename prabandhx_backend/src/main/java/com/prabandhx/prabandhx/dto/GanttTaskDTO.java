package com.prabandhx.prabandhx.dto;

import java.time.LocalDateTime;
import java.util.List;

public class GanttTaskDTO {
    private Long id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer progress;
    private Boolean isMilestone;
    private Integer durationDays;
    private String color;
    private Long projectId;
    private String projectName;
    private Long assignedToId;
    private String assignedToName;
    private List<Long> predecessorIds;
    private List<Long> successorIds;

    public GanttTaskDTO() {}

    public GanttTaskDTO(Long id, String title, String description, String status, String priority,
                        LocalDateTime startDate, LocalDateTime endDate, Integer progress,
                        Boolean isMilestone, Integer durationDays, String color, Long projectId,
                        String projectName, Long assignedToId, String assignedToName,
                        List<Long> predecessorIds, List<Long> successorIds) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.startDate = startDate;
        this.endDate = endDate;
        this.progress = progress;
        this.isMilestone = isMilestone;
        this.durationDays = durationDays;
        this.color = color;
        this.projectId = projectId;
        this.projectName = projectName;
        this.assignedToId = assignedToId;
        this.assignedToName = assignedToName;
        this.predecessorIds = predecessorIds;
        this.successorIds = successorIds;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }

    public Boolean getIsMilestone() { return isMilestone; }
    public void setIsMilestone(Boolean isMilestone) { this.isMilestone = isMilestone; }

    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public Long getAssignedToId() { return assignedToId; }
    public void setAssignedToId(Long assignedToId) { this.assignedToId = assignedToId; }

    public String getAssignedToName() { return assignedToName; }
    public void setAssignedToName(String assignedToName) { this.assignedToName = assignedToName; }

    public List<Long> getPredecessorIds() { return predecessorIds; }
    public void setPredecessorIds(List<Long> predecessorIds) { this.predecessorIds = predecessorIds; }

    public List<Long> getSuccessorIds() { return successorIds; }
    public void setSuccessorIds(List<Long> successorIds) { this.successorIds = successorIds; }
}