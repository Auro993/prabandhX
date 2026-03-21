package com.prabandhx.prabandhx.repository;

import com.prabandhx.prabandhx.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // ===============================
    // EXISTING QUERIES (KEEP ALL OF THESE)
    // ===============================
    
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId")
    List<Task> findByProjectId(@Param("projectId") Long projectId);
    
    @Query("SELECT t FROM Task t WHERE t.assignedTo.id = :userId")
    List<Task> findByAssignedToId(@Param("userId") Long userId);
    
    @Query("SELECT t FROM Task t WHERE t.status = :status")
    List<Task> findByStatus(@Param("status") String status);
    
    @Query("SELECT t FROM Task t WHERE t.priority = :priority")
    List<Task> findByPriority(@Param("priority") String priority);
    
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.status = :status")
    List<Task> findByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") String status);
    
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.assignedTo.id = :userId")
    List<Task> findByProjectIdAndAssignedToId(@Param("projectId") Long projectId, @Param("userId") Long userId);
    
    @Query("SELECT t FROM Task t WHERE t.assignedTo.id = :userId AND t.status = :status")
    List<Task> findByAssignedToIdAndStatus(@Param("userId") Long userId, @Param("status") String status);
    
    @Query("SELECT t FROM Task t WHERE t.dueDate < :date")
    List<Task> findByDueDateBefore(@Param("date") LocalDateTime date);
    
    @Query("SELECT t FROM Task t WHERE t.dueDate < :currentDate AND t.status != 'COMPLETED'")
    List<Task> findOverdueTasks(@Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId")
    Long countByProjectId(@Param("projectId") Long projectId);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = :status")
    Long countByStatus(@Param("status") String status);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedTo.id = :userId")
    Long countByAssignedToId(@Param("userId") Long userId);
    
    @Query("SELECT t.status, COUNT(t) FROM Task t WHERE t.project.id = :projectId GROUP BY t.status")
    List<Object[]> getTaskStatsByProject(@Param("projectId") Long projectId);
    
    // ===============================
    // NEW QUERIES FOR DEADLINE FEATURES
    // ===============================
    
    @Query("SELECT t FROM Task t WHERE t.dueDate < :date AND t.status != :status")
    List<Task> findTasksDueBeforeAndNotCompleted(@Param("date") LocalDateTime date, @Param("status") String status);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedTo.id = :userId AND t.dueDate < :date AND t.status != :status")
    Long countOverdueTasksForUser(@Param("userId") Long userId, @Param("date") LocalDateTime date, @Param("status") String status);
    
    @Query("SELECT t FROM Task t WHERE t.dueDate BETWEEN :start AND :end")
    List<Task> findByDueDateBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT t FROM Task t WHERE t.assignedTo.id = :userId AND t.dueDate < :currentDate AND t.status != 'COMPLETED'")
    List<Task> findOverdueTasksByUser(@Param("userId") Long userId, @Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT t FROM Task t WHERE t.assignedTo.id = :userId AND t.dueDate BETWEEN :start AND :end AND t.status != 'COMPLETED'")
    List<Task> findDueTodayByUser(@Param("userId") Long userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT t FROM Task t WHERE t.assignedTo.id = :userId AND t.status != 'COMPLETED' ORDER BY t.dueDate ASC")
    List<Task> findUpcomingTasksByUser(@Param("userId") Long userId);
    
    // ===============================
    // NEW QUERIES FOR GANTT CHART
    // ===============================
    
    /**
     * Get all tasks for a project ordered by start date for Gantt chart
     */
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId ORDER BY t.startDate ASC NULLS LAST, t.createdAt ASC")
    List<Task> findByProjectIdOrderByStartDateAsc(@Param("projectId") Long projectId);
    
    /**
     * Get tasks with start and end dates (scheduled tasks) for Gantt
     */
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.startDate IS NOT NULL AND t.endDate IS NOT NULL ORDER BY t.startDate ASC")
    List<Task> findScheduledTasksByProject(@Param("projectId") Long projectId);
    
    /**
     * Get milestone tasks for Gantt chart
     */
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.isMilestone = true ORDER BY t.startDate ASC")
    List<Task> findMilestonesByProject(@Param("projectId") Long projectId);
    
    /**
     * Get tasks that are overdue based on end date (for Gantt)
     */
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.endDate < :currentDate AND t.status != 'COMPLETED'")
    List<Task> findOverdueTasksByEndDate(@Param("projectId") Long projectId, @Param("currentDate") LocalDateTime currentDate);
    
    /**
     * Get tasks within a date range (for Gantt timeline)
     */
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND ((t.startDate BETWEEN :start AND :end) OR (t.endDate BETWEEN :start AND :end) OR (t.startDate <= :start AND t.endDate >= :end))")
    List<Task> findTasksInDateRange(@Param("projectId") Long projectId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    /**
     * Get tasks that are currently in progress (for Gantt)
     */
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.status = 'IN_PROGRESS' AND t.startDate IS NOT NULL")
    List<Task> findInProgressTasksByProject(@Param("projectId") Long projectId);
    
    /**
     * Get tasks that are blocked by dependencies (not started because predecessor not complete)
     */
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.status = 'TODO' AND t.id IN " +
           "(SELECT td.successorTask.id FROM TaskDependency td WHERE td.successorTask.id = t.id AND td.predecessorTask.status != 'COMPLETED')")
    List<Task> findBlockedTasksByProject(@Param("projectId") Long projectId);
    
    /**
     * Get task counts by status for Gantt overview
     */
    @Query("SELECT t.status, COUNT(t) FROM Task t WHERE t.project.id = :projectId GROUP BY t.status")
    List<Object[]> getTaskStatusCountsForGantt(@Param("projectId") Long projectId);
    
    /**
     * Get upcoming tasks (next 30 days) for Gantt planning
     */
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.startDate BETWEEN :today AND :future ORDER BY t.startDate ASC")
    List<Task> findUpcomingTasksForGantt(@Param("projectId") Long projectId, @Param("today") LocalDateTime today, @Param("future") LocalDateTime future);
}