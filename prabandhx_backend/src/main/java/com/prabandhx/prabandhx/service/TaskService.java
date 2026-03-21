package com.prabandhx.prabandhx.service;

import com.prabandhx.prabandhx.dto.TaskDTO;
import com.prabandhx.prabandhx.entity.Task;
import com.prabandhx.prabandhx.entity.Project;
import com.prabandhx.prabandhx.entity.User;
import com.prabandhx.prabandhx.repository.TaskRepository;
import com.prabandhx.prabandhx.repository.ProjectRepository;
import com.prabandhx.prabandhx.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ActivityLogService activityLogService;

    // ===============================
    // CONVERTER METHODS
    // ===============================
    
    private TaskDTO convertToDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        dto.setStoryPoints(task.getStoryPoints());
        
        // Handle dates
        if (task.getDueDate() != null) {
            dto.setDueDate(task.getDueDate().toLocalDate());
        }
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        dto.setCompletedAt(task.getCompletedAt());
        
        // Project info
        if (task.getProject() != null) {
            dto.setProjectId(task.getProject().getId());
            dto.setProjectName(task.getProject().getName());
        }
        
        // Assigned user info
        if (task.getAssignedTo() != null) {
            dto.setAssignedToUserId(task.getAssignedTo().getId());
            dto.setAssignedToName(task.getAssignedTo().getName());
            dto.setAssignedToEmail(task.getAssignedTo().getEmail());
        }
        
        // Calculate overdue status
        dto.setOverdue(task.isOverdue());
        
        // Calculate days until deadline
        if (task.getDueDate() != null) {
            long days = java.time.Duration.between(
                LocalDateTime.now(), 
                task.getDueDate()
            ).toDays();
            dto.setDaysUntilDeadline(days);
        }
        
        return dto;
    }
    
    private Task convertToEntity(TaskDTO dto, Project project, User assignedUser) {
        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setPriority(dto.getPriority());
        task.setStatus(dto.getStatus() != null ? dto.getStatus() : "TODO");
        task.setStoryPoints(dto.getStoryPoints());
        
        // Convert LocalDate to LocalDateTime (start of day)
        if (dto.getDueDate() != null) {
            task.setDueDate(dto.getDueDate().atStartOfDay());
        }
        
        task.setProject(project);
        task.setAssignedTo(assignedUser);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        
        return task;
    }

    // ===============================
    // CREATE
    // ===============================

    @Transactional
    public TaskDTO createTask(TaskDTO taskDTO, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));
        
        User assignedUser = null;
        Long assignedUserId = null;
        
        // FIX: Try to find user by ID first
        if (taskDTO.getAssignedToUserId() != null) {
            assignedUser = userRepository.findById(taskDTO.getAssignedToUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + taskDTO.getAssignedToUserId()));
            assignedUserId = assignedUser.getId();
        }
        // If no ID, try to find by email
        else if (taskDTO.getAssignedToEmail() != null && !taskDTO.getAssignedToEmail().isEmpty()) {
            assignedUser = userRepository.findByEmail(taskDTO.getAssignedToEmail())
                    .orElse(null);
            
            if (assignedUser != null) {
                System.out.println("✅ Found user by email: " + assignedUser.getEmail() + " with ID: " + assignedUser.getId());
                // Set the ID back to DTO for response
                taskDTO.setAssignedToUserId(assignedUser.getId());
                assignedUserId = assignedUser.getId();
            } else {
                System.out.println("⚠️ No user found with email: " + taskDTO.getAssignedToEmail());
            }
        }
        
        Task task = convertToEntity(taskDTO, project, assignedUser);
        
        if (task.getStatus() == null) task.setStatus("TODO");
        if (task.getPriority() == null) task.setPriority("MEDIUM");
        
        Task savedTask = taskRepository.save(task);
        
        // ✅ LOG ACTIVITY: Task Created
        activityLogService.logActivity(
            assignedUserId != null ? assignedUserId : 0L,
            "CREATE_TASK",
            "Created task",
            "TASK",
            savedTask.getId(),
            savedTask.getTitle(),
            projectId,
            "Task '" + savedTask.getTitle() + "' created in project '" + project.getName() + "'" + 
            (assignedUser != null ? " and assigned to " + assignedUser.getName() : ""),
            null
        );
        
        return convertToDTO(savedTask);
    }

    // ===============================
    // READ
    // ===============================

    public List<TaskDTO> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public TaskDTO getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        return convertToDTO(task);
    }

    public List<TaskDTO> getTasksByProject(Long projectId) {
        return taskRepository.findByProjectId(projectId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<TaskDTO> getTasksByAssignee(Long userId) {
        return taskRepository.findByAssignedToId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<TaskDTO> getTasksByStatus(String status) {
        return taskRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<TaskDTO> getTasksByProjectAndStatus(Long projectId, String status) {
        return taskRepository.findByProjectIdAndStatus(projectId, status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<TaskDTO> getTasksByProjectAndAssignee(Long projectId, Long userId) {
        return taskRepository.findByProjectIdAndAssignedToId(projectId, userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    // ===============================
    // DEADLINE SPECIFIC METHODS
    // ===============================
    
    public List<TaskDTO> getOverdueTasks() {
        return taskRepository.findTasksDueBeforeAndNotCompleted(LocalDateTime.now(), "COMPLETED").stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<TaskDTO> getTasksDueToday() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        return taskRepository.findByDueDateBetween(startOfDay, endOfDay).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<TaskDTO> getTasksDueThisWeek() {
        LocalDateTime startOfWeek = LocalDate.now().atStartOfDay();
        LocalDateTime endOfWeek = LocalDate.now().plusDays(7).atTime(LocalTime.MAX);
        return taskRepository.findByDueDateBetween(startOfWeek, endOfWeek).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<TaskDTO> getOverdueTasksByUser(Long userId) {
        return taskRepository.findOverdueTasksByUser(userId, LocalDateTime.now()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public Long countOverdueTasksByUser(Long userId) {
        return taskRepository.countOverdueTasksForUser(userId, LocalDateTime.now(), "COMPLETED");
    }
    
    public List<TaskDTO> getTasksDueTodayForUser(Long userId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        return taskRepository.findDueTodayByUser(userId, startOfDay, endOfDay).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<TaskDTO> getUpcomingTasksForUser(Long userId) {
        return taskRepository.findUpcomingTasksByUser(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ===============================
    // UPDATE
    // ===============================

    @Transactional
    public TaskDTO updateTask(Long id, TaskDTO taskDTO) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

        String oldTitle = task.getTitle();
        String oldStatus = task.getStatus();
        String oldPriority = task.getPriority();
        User oldAssignee = task.getAssignedTo();
        
        if (taskDTO.getTitle() != null) task.setTitle(taskDTO.getTitle());
        if (taskDTO.getDescription() != null) task.setDescription(taskDTO.getDescription());
        if (taskDTO.getPriority() != null) task.setPriority(taskDTO.getPriority());
        if (taskDTO.getStatus() != null) {
            task.setStatus(taskDTO.getStatus());
            // completedAt is auto-set in Task entity's setStatus method
        }
        if (taskDTO.getDueDate() != null) {
            task.setDueDate(taskDTO.getDueDate().atStartOfDay());
        }
        if (taskDTO.getStoryPoints() != null) task.setStoryPoints(taskDTO.getStoryPoints());
        
        // Update assigned user if changed
        Long newAssigneeId = null;
        if (taskDTO.getAssignedToUserId() != null && 
            (task.getAssignedTo() == null || !task.getAssignedTo().getId().equals(taskDTO.getAssignedToUserId()))) {
            User user = userRepository.findById(taskDTO.getAssignedToUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + taskDTO.getAssignedToUserId()));
            task.setAssignedTo(user);
            newAssigneeId = user.getId();
        }

        task.setUpdatedAt(LocalDateTime.now());
        Task updatedTask = taskRepository.save(task);
        
        // ✅ LOG ACTIVITY: Task Updated
        String changes = "";
        if (!oldTitle.equals(task.getTitle())) changes += "title, ";
        if (!oldStatus.equals(task.getStatus())) changes += "status, ";
        if (!oldPriority.equals(task.getPriority())) changes += "priority, ";
        if (newAssigneeId != null) changes += "assignee, ";
        
        activityLogService.logActivity(
            task.getAssignedTo() != null ? task.getAssignedTo().getId() : null,
            "UPDATE_TASK",
            "Updated task",
            "TASK",
            updatedTask.getId(),
            updatedTask.getTitle(),
            updatedTask.getProject().getId(),
            "Task '" + updatedTask.getTitle() + "' updated. Modified: " + 
                (changes.isEmpty() ? "no changes" : changes.substring(0, changes.length() - 2)),
            null
        );

        return convertToDTO(updatedTask);
    }

    // ===============================
    // UPDATED: TASK STATUS UPDATE WITH POINTS
    // ===============================

    @Transactional
    public TaskDTO updateTaskStatus(Long id, String status) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        
        String oldStatus = task.getStatus();
        
        // Only process if status is actually changing
        if (oldStatus.equals(status)) {
            return convertToDTO(task);
        }
        
        task.setStatus(status);
        task.setUpdatedAt(LocalDateTime.now());
        
        // If task is being marked as COMPLETED, award points
        boolean pointsAwarded = false;
        int pointsEarned = 0;
        if ("COMPLETED".equals(status) && !"COMPLETED".equals(oldStatus)) {
            pointsEarned = awardPointsForCompletion(task);
            pointsAwarded = true;
        }
        
        // If task was completed but now being un-completed (optional)
        if (!"COMPLETED".equals(status) && "COMPLETED".equals(oldStatus)) {
            deductPointsForReopening(task);
        }
        
        Task updatedTask = taskRepository.save(task);
        
        // ✅ LOG ACTIVITY: Task Status Changed
        String statusMessage = "Task status changed from " + oldStatus + " to " + status;
        if (pointsAwarded) {
            statusMessage += " and awarded " + pointsEarned + " points";
        }
        
        activityLogService.logActivity(
            task.getAssignedTo() != null ? task.getAssignedTo().getId() : null,
            "UPDATE_TASK_STATUS",
            "Updated task status",
            "TASK",
            updatedTask.getId(),
            updatedTask.getTitle(),
            updatedTask.getProject().getId(),
            statusMessage,
            null
        );

        return convertToDTO(updatedTask);
    }

    // ===============================
    // POINTS AND STREAK METHODS
    // ===============================

    /**
     * Award points to user when they complete a task
     */
    private int awardPointsForCompletion(Task task) {
        User assignedUser = task.getAssignedTo();
        if (assignedUser == null) {
            System.out.println("⚠️ Task not assigned to any user, no points awarded");
            return 0;
        }
        
        LocalDateTime now = LocalDateTime.now();
        boolean isOnTime = task.getDueDate() != null && now.isBefore(task.getDueDate());
        
        // Base points (use storyPoints if available, otherwise default to 10)
        int basePoints = task.getStoryPoints() != null ? task.getStoryPoints() : 10;
        
        // Calculate points earned
        int pointsEarned;
        
        if (isOnTime) {
            // ON-TIME COMPLETION: Full points + streak bonus
            pointsEarned = basePoints;
            
            // Update streak
            updateStreak(assignedUser, now, true);
            
            // Check for streak bonus (every 5 tasks)
            if (assignedUser.getCurrentStreak() > 0 && 
                assignedUser.getCurrentStreak() % 5 == 0) {
                int bonusPoints = 25;
                pointsEarned += bonusPoints;
                System.out.println("🎉 STREAK BONUS! +" + bonusPoints + " points for " + 
                                 assignedUser.getCurrentStreak() + " task streak");
            }
        } else {
            // LATE COMPLETION: Half points, streak reset
            pointsEarned = basePoints / 2;
            
            // Reset streak for late completion
            updateStreak(assignedUser, now, false);
        }
        
        // Ensure at least 1 point for late completion
        if (pointsEarned < 1) {
            pointsEarned = 1;
        }
        
        // Update user's total points
        assignedUser.setTotalPoints(assignedUser.getTotalPoints() + pointsEarned);
        assignedUser.setLastCompletedDate(now);
        
        // Save user
        userRepository.save(assignedUser);
        
        // Log the award to activity log
        activityLogService.logActivity(
            assignedUser.getId(),
            "TASK_COMPLETED",
            "Completed task",
            "TASK",
            task.getId(),
            task.getTitle(),
            task.getProject().getId(),
            "Completed task '" + task.getTitle() + "' and earned " + pointsEarned + " points" +
                (isOnTime ? " (on-time)" : " (late)"),
            null
        );
        
        System.out.println("✅ POINTS AWARDED: " + pointsEarned + " points to " + 
                          assignedUser.getEmail() + " for task: " + task.getTitle());
        
        return pointsEarned;
    }

    /**
     * Update user's streak based on completion
     */
    private void updateStreak(User user, LocalDateTime completionDate, boolean isOnTime) {
        if (!isOnTime) {
            // Late completion resets streak
            user.setCurrentStreak(0);
            return;
        }
        
        // On-time completion - check for consecutive days
        if (user.getLastCompletedDate() != null) {
            LocalDateTime lastDate = user.getLastCompletedDate();
            LocalDateTime nextDay = lastDate.plusDays(1);
            
            // Check if completed on consecutive day
            if (completionDate.toLocalDate().equals(nextDay.toLocalDate())) {
                // Consecutive day - increase streak
                user.setCurrentStreak(user.getCurrentStreak() + 1);
            } else if (!completionDate.toLocalDate().equals(lastDate.toLocalDate())) {
                // Not consecutive and not same day - reset streak to 1
                user.setCurrentStreak(1);
            }
            // If same day (multiple tasks), don't change streak
        } else {
            // First completed task
            user.setCurrentStreak(1);
        }
        
        // Update longest streak if current is longer
        if (user.getCurrentStreak() > user.getLongestStreak()) {
            user.setLongestStreak(user.getCurrentStreak());
        }
    }

    /**
     * Optional: Deduct points when task is reopened
     */
    private void deductPointsForReopening(Task task) {
        User assignedUser = task.getAssignedTo();
        if (assignedUser == null) return;
        
        // Deduct half the points (optional business rule)
        int basePoints = task.getStoryPoints() != null ? task.getStoryPoints() : 10;
        int pointsToDeduct = basePoints / 2;
        
        assignedUser.setTotalPoints(Math.max(0, assignedUser.getTotalPoints() - pointsToDeduct));
        userRepository.save(assignedUser);
        
        // Log the deduction
        activityLogService.logActivity(
            assignedUser.getId(),
            "TASK_REOPENED",
            "Task reopened",
            "TASK",
            task.getId(),
            task.getTitle(),
            task.getProject().getId(),
            "Task reopened, deducted " + pointsToDeduct + " points",
            null
        );
        
        System.out.println("⚠️ POINTS DEDUCTED: " + pointsToDeduct + " from " + 
                          assignedUser.getEmail() + " (task reopened)");
    }

    // ===============================
    // ASSIGN TASK TO USER
    // ===============================

    @Transactional
    public TaskDTO assignTaskToUser(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        User oldAssignee = task.getAssignedTo();
        task.setAssignedTo(user);
        task.setUpdatedAt(LocalDateTime.now());
        
        Task updatedTask = taskRepository.save(task);
        
        // ✅ LOG ACTIVITY: Task Assigned
        activityLogService.logActivity(
            userId,
            "ASSIGN_TASK",
            "Task assigned",
            "TASK",
            updatedTask.getId(),
            updatedTask.getTitle(),
            updatedTask.getProject().getId(),
            "Task '" + updatedTask.getTitle() + "' assigned to " + user.getName() +
                (oldAssignee != null ? " (was assigned to " + oldAssignee.getName() + ")" : ""),
            null
        );
        
        return convertToDTO(updatedTask);
    }

    // ===============================
    // DELETE
    // ===============================

    @Transactional
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        
        String taskTitle = task.getTitle();
        Long projectId = task.getProject() != null ? task.getProject().getId() : null;
        Long assignedUserId = task.getAssignedTo() != null ? task.getAssignedTo().getId() : null;
        
        taskRepository.deleteById(id);
        
        // ✅ LOG ACTIVITY: Task Deleted
        activityLogService.logActivity(
            assignedUserId,
            "DELETE_TASK",
            "Deleted task",
            "TASK",
            id,
            taskTitle,
            projectId,
            "Task '" + taskTitle + "' was deleted",
            null
        );
    }

    // ===============================
    // UTILITY / COUNT METHODS
    // ===============================

    public Long countTasksByProject(Long projectId) {
        return taskRepository.countByProjectId(projectId);
    }
    
    public Long countTasksByStatus(String status) {
        return taskRepository.countByStatus(status);
    }
    
    public Long countTasksByUser(Long userId) {
        return taskRepository.countByAssignedToId(userId);
    }
    
    public List<Object[]> getTaskStatisticsByProject(Long projectId) {
        return taskRepository.getTaskStatsByProject(projectId);
    }
}