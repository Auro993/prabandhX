package com.prabandhx.prabandhx.service;

import com.prabandhx.prabandhx.dto.GanttChartDataDTO;
import com.prabandhx.prabandhx.dto.GanttMilestoneDTO;
import com.prabandhx.prabandhx.dto.GanttTaskDTO;
import com.prabandhx.prabandhx.entity.*;
import com.prabandhx.prabandhx.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GanttChartService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private MilestoneRepository milestoneRepository;

    @Autowired
    private TaskDependencyRepository dependencyRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Get complete Gantt chart data for a project
     */
    public GanttChartDataDTO getGanttData(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        // Get all tasks for the project
        List<Task> tasks = taskRepository.findByProjectIdOrderByStartDateAsc(projectId);

        // Get all milestones for the project
        List<Milestone> milestones = milestoneRepository.findByProjectId(projectId);

        // Get all dependencies for the project
        List<TaskDependency> dependencies = dependencyRepository.findByProjectId(projectId);

        // Build dependency maps
        Map<Long, List<Long>> predecessorMap = buildPredecessorMap(dependencies);
        Map<Long, List<Long>> successorMap = buildSuccessorMap(dependencies);

        // Convert tasks to DTOs
        List<GanttTaskDTO> taskDTOs = tasks.stream()
                .map(task -> convertToGanttTaskDTO(task, predecessorMap, successorMap))
                .collect(Collectors.toList());

        // Convert milestones to DTOs
        List<GanttMilestoneDTO> milestoneDTOs = milestones.stream()
                .map(this::convertToGanttMilestoneDTO)
                .collect(Collectors.toList());

        // Get project settings
        Map<String, Object> settings = getProjectSettings(project);

        // Calculate project progress
        int projectProgress = calculateProjectProgress(tasks);

        return new GanttChartDataDTO(
                project.getId(),
                project.getName(),
                project.getStartDate(),
                project.getEndDate(),
                projectProgress,
                taskDTOs,
                milestoneDTOs,
                settings
        );
    }

    /**
     * Update task dates (for drag & drop rescheduling)
     */
    @Transactional
    public GanttTaskDTO updateTaskDates(Long taskId, LocalDateTime newStartDate, LocalDateTime newEndDate) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        task.setStartDate(newStartDate);
        task.setEndDate(newEndDate);
        
        // Recalculate duration
        if (newStartDate != null && newEndDate != null) {
            long days = java.time.Duration.between(newStartDate, newEndDate).toDays();
            task.setDurationDays((int) Math.max(days, 1));
        }

        Task savedTask = taskRepository.save(task);
        
        // Get dependencies
        List<TaskDependency> dependencies = dependencyRepository.findBySuccessorTaskId(taskId);
        Map<Long, List<Long>> predecessorMap = buildPredecessorMap(dependencies);
        Map<Long, List<Long>> successorMap = buildSuccessorMap(dependencies);

        return convertToGanttTaskDTO(savedTask, predecessorMap, successorMap);
    }

    /**
     * Update task progress
     */
    @Transactional
    public GanttTaskDTO updateTaskProgress(Long taskId, Integer progress) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        task.setProgress(progress);
        
        // Auto-complete if progress reaches 100
        if (progress != null && progress == 100 && !task.isCompleted()) {
            task.setStatus("COMPLETED");
            task.setCompletedAt(LocalDateTime.now());
        }

        Task savedTask = taskRepository.save(task);
        
        List<TaskDependency> dependencies = dependencyRepository.findBySuccessorTaskId(taskId);
        Map<Long, List<Long>> predecessorMap = buildPredecessorMap(dependencies);
        Map<Long, List<Long>> successorMap = buildSuccessorMap(dependencies);

        return convertToGanttTaskDTO(savedTask, predecessorMap, successorMap);
    }

    /**
     * Create a task dependency
     */
    @Transactional
    public TaskDependency createDependency(Long predecessorId, Long successorId, 
                                           TaskDependency.DependencyType type, Integer lagDays) {
        // Check if dependency already exists
        if (dependencyRepository.existsByPredecessorAndSuccessor(predecessorId, successorId)) {
            throw new RuntimeException("Dependency already exists between these tasks");
        }

        Task predecessor = taskRepository.findById(predecessorId)
                .orElseThrow(() -> new RuntimeException("Predecessor task not found"));
        Task successor = taskRepository.findById(successorId)
                .orElseThrow(() -> new RuntimeException("Successor task not found"));

        // Check for circular dependencies
        if (wouldCreateCircularDependency(predecessorId, successorId)) {
            throw new RuntimeException("This dependency would create a circular reference");
        }

        TaskDependency dependency = new TaskDependency(predecessor, successor, type, lagDays);
        return dependencyRepository.save(dependency);
    }

    /**
     * Delete a task dependency
     */
    @Transactional
    public void deleteDependency(Long dependencyId) {
        dependencyRepository.deleteById(dependencyId);
    }

    /**
     * Create a milestone
     */
    @Transactional
    public Milestone createMilestone(Long projectId, String title, LocalDateTime dueDate, 
                                     String description, String color, String icon) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        Milestone milestone = new Milestone(title, project, dueDate);
        milestone.setDescription(description);
        if (color != null) milestone.setColor(color);
        if (icon != null) milestone.setIcon(icon);
        
        return milestoneRepository.save(milestone);
    }

    /**
     * Update milestone completion status
     */
    @Transactional
    public Milestone updateMilestoneStatus(Long milestoneId, Boolean isCompleted) {
        Milestone milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new RuntimeException("Milestone not found"));
        
        milestone.setIsCompleted(isCompleted);
        return milestoneRepository.save(milestone);
    }

    /**
     * Update project Gantt settings
     */
    @Transactional
    public Map<String, Object> updateGanttSettings(Long projectId, Map<String, Object> settings) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        
        try {
            if (settings.containsKey("zoomLevel")) {
                project.setGanttZoomLevel((String) settings.get("zoomLevel"));
            }
            if (settings.containsKey("showWeekends")) {
                project.setGanttShowWeekends((Boolean) settings.get("showWeekends"));
            }
            if (settings.containsKey("rowHeight")) {
                project.setGanttRowHeight((Integer) settings.get("rowHeight"));
            }
            
            project.setGanttSettings(objectMapper.writeValueAsString(settings));
            projectRepository.save(project);
            
            return settings;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save Gantt settings", e);
        }
    }

    // ===== PRIVATE HELPER METHODS =====

    private GanttTaskDTO convertToGanttTaskDTO(Task task, Map<Long, List<Long>> predecessorMap, 
                                                Map<Long, List<Long>> successorMap) {
        return new GanttTaskDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getStartDate(),
                task.getEndDate(),
                task.getProgress(),
                task.getIsMilestone(),
                task.getDurationDays(),
                task.getGanttColor(),
                task.getProjectId(),
                task.getProjectName(),
                task.getAssignedToId(),
                task.getAssignedToName(),
                predecessorMap.getOrDefault(task.getId(), new ArrayList<>()),
                successorMap.getOrDefault(task.getId(), new ArrayList<>())
        );
    }

    private GanttMilestoneDTO convertToGanttMilestoneDTO(Milestone milestone) {
        return new GanttMilestoneDTO(
                milestone.getId(),
                milestone.getTitle(),
                milestone.getDescription(),
                milestone.getDueDate(),
                milestone.getIsCompleted(),
                milestone.getColor(),
                milestone.getIcon(),
                milestone.getProjectId(),
                milestone.getProjectName()
        );
    }

    private Map<Long, List<Long>> buildPredecessorMap(List<TaskDependency> dependencies) {
        Map<Long, List<Long>> map = new HashMap<>();
        for (TaskDependency dep : dependencies) {
            Long successorId = dep.getSuccessorTaskId();
            Long predecessorId = dep.getPredecessorTaskId();
            map.computeIfAbsent(successorId, k -> new ArrayList<>()).add(predecessorId);
        }
        return map;
    }

    private Map<Long, List<Long>> buildSuccessorMap(List<TaskDependency> dependencies) {
        Map<Long, List<Long>> map = new HashMap<>();
        for (TaskDependency dep : dependencies) {
            Long predecessorId = dep.getPredecessorTaskId();
            Long successorId = dep.getSuccessorTaskId();
            map.computeIfAbsent(predecessorId, k -> new ArrayList<>()).add(successorId);
        }
        return map;
    }

    private Map<String, Object> getProjectSettings(Project project) {
        Map<String, Object> settings = new HashMap<>();
        settings.put("zoomLevel", project.getGanttZoomLevel());
        settings.put("showWeekends", project.getGanttShowWeekends());
        settings.put("rowHeight", project.getGanttRowHeight());
        
        try {
            if (project.getGanttSettings() != null && !project.getGanttSettings().isEmpty()) {
                Map<String, Object> savedSettings = objectMapper.readValue(
                    project.getGanttSettings(), 
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
                );
                settings.putAll(savedSettings);
            }
        } catch (Exception e) {
            // Ignore JSON parsing errors
        }
        
        return settings;
    }

    private int calculateProjectProgress(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return 0;
        }
        
        int totalProgress = 0;
        int validTasks = 0;
        
        for (Task task : tasks) {
            if (task != null && !Boolean.TRUE.equals(task.getIsMilestone())) {
                Integer progress = task.getProgress();
                if (progress != null) {
                    totalProgress += progress;
                    validTasks++;
                }
            }
        }
        
        return validTasks == 0 ? 0 : totalProgress / validTasks;
    }

    private boolean wouldCreateCircularDependency(Long predecessorId, Long successorId) {
        // Simple check - if successor already has predecessor as a successor somewhere in chain
        Set<Long> visited = new HashSet<>();
        return hasCircularDependency(successorId, predecessorId, visited);
    }

    private boolean hasCircularDependency(Long currentId, Long targetId, Set<Long> visited) {
        if (currentId.equals(targetId)) {
            return true;
        }
        
        if (visited.contains(currentId)) {
            return false;
        }
        
        visited.add(currentId);
        
        List<TaskDependency> dependencies = dependencyRepository.findByPredecessorTaskId(currentId);
        for (TaskDependency dep : dependencies) {
            if (hasCircularDependency(dep.getSuccessorTaskId(), targetId, visited)) {
                return true;
            }
        }
        
        return false;
    }
}