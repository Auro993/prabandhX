package com.prabandhx.prabandhx.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class GanttChartDataDTO {
    private Long projectId;
    private String projectName;
    private LocalDateTime projectStartDate;
    private LocalDateTime projectEndDate;
    private Integer projectProgress;
    private List<GanttTaskDTO> tasks;
    private List<GanttMilestoneDTO> milestones;
    private Map<String, Object> settings;

    public GanttChartDataDTO() {}

    public GanttChartDataDTO(Long projectId, String projectName, LocalDateTime projectStartDate,
                             LocalDateTime projectEndDate, Integer projectProgress,
                             List<GanttTaskDTO> tasks, List<GanttMilestoneDTO> milestones,
                             Map<String, Object> settings) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.projectStartDate = projectStartDate;
        this.projectEndDate = projectEndDate;
        this.projectProgress = projectProgress;
        this.tasks = tasks;
        this.milestones = milestones;
        this.settings = settings;
    }

    // Getters and Setters
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public LocalDateTime getProjectStartDate() { return projectStartDate; }
    public void setProjectStartDate(LocalDateTime projectStartDate) { this.projectStartDate = projectStartDate; }

    public LocalDateTime getProjectEndDate() { return projectEndDate; }
    public void setProjectEndDate(LocalDateTime projectEndDate) { this.projectEndDate = projectEndDate; }

    public Integer getProjectProgress() { return projectProgress; }
    public void setProjectProgress(Integer projectProgress) { this.projectProgress = projectProgress; }

    public List<GanttTaskDTO> getTasks() { return tasks; }
    public void setTasks(List<GanttTaskDTO> tasks) { this.tasks = tasks; }

    public List<GanttMilestoneDTO> getMilestones() { return milestones; }
    public void setMilestones(List<GanttMilestoneDTO> milestones) { this.milestones = milestones; }

    public Map<String, Object> getSettings() { return settings; }
    public void setSettings(Map<String, Object> settings) { this.settings = settings; }
}