package com.prabandhx.prabandhx.repository;

import com.prabandhx.prabandhx.entity.Milestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MilestoneRepository extends JpaRepository<Milestone, Long> {

    /**
     * Find all milestones for a project
     */
    @Query("SELECT m FROM Milestone m WHERE m.project.id = :projectId ORDER BY m.dueDate ASC")
    List<Milestone> findByProjectId(@Param("projectId") Long projectId);

    /**
     * Find upcoming milestones (due date after current date, not completed)
     */
    @Query("SELECT m FROM Milestone m WHERE m.project.id = :projectId AND m.dueDate > :currentDate AND m.isCompleted = false ORDER BY m.dueDate ASC")
    List<Milestone> findUpcomingMilestones(@Param("projectId") Long projectId, @Param("currentDate") LocalDateTime currentDate);

    /**
     * Find overdue milestones
     */
    @Query("SELECT m FROM Milestone m WHERE m.project.id = :projectId AND m.dueDate < :currentDate AND m.isCompleted = false")
    List<Milestone> findOverdueMilestones(@Param("projectId") Long projectId, @Param("currentDate") LocalDateTime currentDate);

    /**
     * Find completed milestones
     */
    @Query("SELECT m FROM Milestone m WHERE m.project.id = :projectId AND m.isCompleted = true ORDER BY m.completedAt DESC")
    List<Milestone> findCompletedMilestones(@Param("projectId") Long projectId);

    /**
     * Count milestones by completion status for a project
     */
    @Query("SELECT COUNT(m) FROM Milestone m WHERE m.project.id = :projectId AND m.isCompleted = :isCompleted")
    Long countByProjectIdAndIsCompleted(@Param("projectId") Long projectId, @Param("isCompleted") Boolean isCompleted);

    /**
     * Get milestone statistics for a project
     * Returns: [totalCount, earliestDueDate, latestDueDate]
     */
    @Query("SELECT COUNT(m), MIN(m.dueDate), MAX(m.dueDate) FROM Milestone m WHERE m.project.id = :projectId")
    List<Object[]> getMilestoneStats(@Param("projectId") Long projectId);
}