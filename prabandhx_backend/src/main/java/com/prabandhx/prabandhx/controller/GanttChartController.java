package com.prabandhx.prabandhx.controller;

import com.prabandhx.prabandhx.dto.GanttChartDataDTO;
import com.prabandhx.prabandhx.dto.GanttTaskDTO;
import com.prabandhx.prabandhx.entity.Milestone;
import com.prabandhx.prabandhx.entity.TaskDependency;
import com.prabandhx.prabandhx.service.GanttChartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/gantt")
@CrossOrigin(origins = "*")
public class GanttChartController {

    @Autowired
    private GanttChartService ganttChartService;

    /**
     * Get complete Gantt chart data for a project
     */
    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<GanttChartDataDTO> getGanttData(@PathVariable Long projectId) {
        GanttChartDataDTO data = ganttChartService.getGanttData(projectId);
        return ResponseEntity.ok(data);
    }

    /**
     * Update task dates (drag & drop rescheduling)
     */
    @PutMapping("/task/{taskId}/dates")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<GanttTaskDTO> updateTaskDates(
            @PathVariable Long taskId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        GanttTaskDTO updatedTask = ganttChartService.updateTaskDates(taskId, startDate, endDate);
        return ResponseEntity.ok(updatedTask);
    }

    /**
     * Update task progress
     */
    @PatchMapping("/task/{taskId}/progress")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<GanttTaskDTO> updateTaskProgress(
            @PathVariable Long taskId,
            @RequestBody Map<String, Integer> request) {
        Integer progress = request.get("progress");
        GanttTaskDTO updatedTask = ganttChartService.updateTaskProgress(taskId, progress);
        return ResponseEntity.ok(updatedTask);
    }

    /**
     * Create task dependency
     */
    @PostMapping("/dependency")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<TaskDependency> createDependency(
            @RequestParam Long predecessorId,
            @RequestParam Long successorId,
            @RequestParam(defaultValue = "FINISH_TO_START") String type,
            @RequestParam(defaultValue = "0") Integer lagDays) {
        
        TaskDependency.DependencyType dependencyType = 
                TaskDependency.DependencyType.valueOf(type.toUpperCase());
        
        TaskDependency dependency = ganttChartService.createDependency(
                predecessorId, successorId, dependencyType, lagDays);
        return ResponseEntity.ok(dependency);
    }

    /**
     * Delete task dependency
     */
    @DeleteMapping("/dependency/{dependencyId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, String>> deleteDependency(@PathVariable Long dependencyId) {
        ganttChartService.deleteDependency(dependencyId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Dependency deleted successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Create milestone
     */
    @PostMapping("/milestone")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Milestone> createMilestone(@RequestBody Map<String, Object> request) {
        Long projectId = ((Number) request.get("projectId")).longValue();
        String title = (String) request.get("title");
        LocalDateTime dueDate = LocalDateTime.parse((String) request.get("dueDate"));
        String description = (String) request.get("description");
        String color = (String) request.get("color");
        String icon = (String) request.get("icon");
        
        Milestone milestone = ganttChartService.createMilestone(
                projectId, title, dueDate, description, color, icon);
        return ResponseEntity.ok(milestone);
    }

    /**
     * Update milestone status
     */
    @PatchMapping("/milestone/{milestoneId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Milestone> updateMilestoneStatus(
            @PathVariable Long milestoneId,
            @RequestBody Map<String, Boolean> request) {
        Milestone milestone = ganttChartService.updateMilestoneStatus(
                milestoneId, request.get("isCompleted"));
        return ResponseEntity.ok(milestone);
    }

    /**
     * Update Gantt settings
     */
    @PutMapping("/project/{projectId}/settings")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> updateGanttSettings(
            @PathVariable Long projectId,
            @RequestBody Map<String, Object> settings) {
        Map<String, Object> updatedSettings = ganttChartService.updateGanttSettings(projectId, settings);
        return ResponseEntity.ok(updatedSettings);
    }
}