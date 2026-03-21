package com.prabandhx.prabandhx.repository;

import com.prabandhx.prabandhx.entity.TaskDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
public interface TaskDependencyRepository extends JpaRepository<TaskDependency, Long> {

    /**
     * Find all dependencies for a specific task (as successor)
     */
    @Query("SELECT d FROM TaskDependency d WHERE d.successorTask.id = :taskId")
    List<TaskDependency> findBySuccessorTaskId(@Param("taskId") Long taskId);

    /**
     * Find all dependencies where task is predecessor
     */
    @Query("SELECT d FROM TaskDependency d WHERE d.predecessorTask.id = :taskId")
    List<TaskDependency> findByPredecessorTaskId(@Param("taskId") Long taskId);

    /**
     * Find all dependencies for a project
     */
    @Query("SELECT d FROM TaskDependency d WHERE d.predecessorTask.project.id = :projectId OR d.successorTask.project.id = :projectId")
    List<TaskDependency> findByProjectId(@Param("projectId") Long projectId);

    /**
     * Check if a dependency already exists
     */
    @Query("SELECT COUNT(d) > 0 FROM TaskDependency d WHERE d.predecessorTask.id = :predecessorId AND d.successorTask.id = :successorId")
    boolean existsByPredecessorAndSuccessor(@Param("predecessorId") Long predecessorId, @Param("successorId") Long successorId);

    /**
     * Delete all dependencies for a task
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM TaskDependency d WHERE d.predecessorTask.id = :taskId OR d.successorTask.id = :taskId")
    void deleteByTaskId(@Param("taskId") Long taskId);

    /**
     * Delete all dependencies for a project
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM TaskDependency d WHERE d.predecessorTask.project.id = :projectId OR d.successorTask.project.id = :projectId")
    void deleteByProjectId(@Param("projectId") Long projectId);
}