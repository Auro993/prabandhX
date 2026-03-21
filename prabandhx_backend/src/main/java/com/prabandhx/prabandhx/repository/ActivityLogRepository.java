package com.prabandhx.prabandhx.repository;

import com.prabandhx.prabandhx.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    // ===== PROJECT-BASED QUERIES =====
    
    @Query("SELECT a FROM ActivityLog a WHERE a.project.id = :projectId ORDER BY a.timestamp DESC")
    List<ActivityLog> findByProjectIdOrderByTimestampDesc(@Param("projectId") Long projectId);

    @Query("SELECT a FROM ActivityLog a WHERE a.project.id = :projectId AND a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<ActivityLog> findRecentActivityByProject(@Param("projectId") Long projectId, @Param("since") LocalDateTime since);

    // This method is used by CollaborationService
    @Query("SELECT a FROM ActivityLog a WHERE a.project.id = :projectId AND a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<ActivityLog> findRecentActivity(@Param("projectId") Long projectId, @Param("since") LocalDateTime since);

    @Query("SELECT a FROM ActivityLog a WHERE a.project.id = :projectId AND a.actionType = :actionType ORDER BY a.timestamp DESC")
    List<ActivityLog> findByProjectIdAndActionType(@Param("projectId") Long projectId, @Param("actionType") String actionType);

    // ===== USER-BASED QUERIES =====
    
    @Query("SELECT a FROM ActivityLog a WHERE a.userEmail = :userEmail ORDER BY a.timestamp DESC")
    List<ActivityLog> findByUserEmailOrderByTimestampDesc(@Param("userEmail") String userEmail);

    @Query("SELECT a FROM ActivityLog a WHERE a.userId = :userId ORDER BY a.timestamp DESC")
    List<ActivityLog> findByUserIdOrderByTimestampDesc(@Param("userId") Long userId);

    @Query("SELECT a FROM ActivityLog a WHERE a.userId = :userId AND a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<ActivityLog> findRecentActivityByUser(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    // ===== ENTITY-BASED QUERIES =====
    
    @Query("SELECT a FROM ActivityLog a WHERE a.entityType = :entityType AND a.entityId = :entityId ORDER BY a.timestamp DESC")
    List<ActivityLog> findByEntity(@Param("entityType") String entityType, @Param("entityId") Long entityId);

    @Query("SELECT a FROM ActivityLog a WHERE a.entityType = :entityType ORDER BY a.timestamp DESC")
    List<ActivityLog> findByEntityType(@Param("entityType") String entityType);

    // ===== ACTION-BASED QUERIES =====
    
    @Query("SELECT a FROM ActivityLog a WHERE a.actionType = :actionType ORDER BY a.timestamp DESC")
    List<ActivityLog> findByActionType(@Param("actionType") String actionType);

    // ===== TIME-BASED QUERIES =====
    
    @Query("SELECT a FROM ActivityLog a WHERE a.timestamp BETWEEN :start AND :end ORDER BY a.timestamp DESC")
    List<ActivityLog> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT a FROM ActivityLog a WHERE DATE(a.timestamp) = CURRENT_DATE ORDER BY a.timestamp DESC")
    List<ActivityLog> findTodayLogs();

    @Query("SELECT a FROM ActivityLog a WHERE a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<ActivityLog> findRecentLogs(@Param("since") LocalDateTime since);

    // ===== RECENT LOGS WITH LIMIT - FIXED: USING JPQL =====
    
    // These methods return all results - we'll limit them in the service layer
    @Query("SELECT a FROM ActivityLog a ORDER BY a.timestamp DESC")
    List<ActivityLog> findAllOrderByTimestampDesc();

    @Query("SELECT a FROM ActivityLog a WHERE a.project.id = :projectId ORDER BY a.timestamp DESC")
    List<ActivityLog> findByProjectIdOrderByTimestampDescWithNoLimit(@Param("projectId") Long projectId);

    @Query("SELECT a FROM ActivityLog a WHERE a.userId = :userId ORDER BY a.timestamp DESC")
    List<ActivityLog> findByUserIdOrderByTimestampDescWithNoLimit(@Param("userId") Long userId);

    // ===== SEARCH QUERIES =====
    
    @Query("SELECT a FROM ActivityLog a WHERE LOWER(a.details) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(a.action) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY a.timestamp DESC")
    List<ActivityLog> searchLogs(@Param("keyword") String keyword);

    @Query("SELECT a FROM ActivityLog a WHERE a.project.id = :projectId AND (LOWER(a.details) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(a.action) LIKE LOWER(CONCAT('%', :keyword, '%'))) ORDER BY a.timestamp DESC")
    List<ActivityLog> searchLogsByProject(@Param("projectId") Long projectId, @Param("keyword") String keyword);

    // ===== STATISTICS QUERIES =====
    
    @Query("SELECT COUNT(a) FROM ActivityLog a WHERE DATE(a.timestamp) = CURRENT_DATE")
    Long getTodayActivityCount();

    @Query("SELECT COUNT(a) FROM ActivityLog a WHERE DATE(a.timestamp) = CURRENT_DATE AND a.project.id = :projectId")
    Long getTodayActivityCountByProject(@Param("projectId") Long projectId);

    @Query("SELECT a.actionType, COUNT(a) FROM ActivityLog a GROUP BY a.actionType")
    List<Object[]> getActivityTypeStats();

    @Query("SELECT a.actionType, COUNT(a) FROM ActivityLog a WHERE a.project.id = :projectId GROUP BY a.actionType")
    List<Object[]> getActivityTypeStatsByProject(@Param("projectId") Long projectId);

    @Query("SELECT DATE(a.timestamp), COUNT(a) FROM ActivityLog a WHERE a.timestamp >= :since GROUP BY DATE(a.timestamp) ORDER BY DATE(a.timestamp)")
    List<Object[]> getActivityTimeline(@Param("since") LocalDateTime since);

    // ===== DELETE OPERATIONS =====
    
    @Modifying
    @Transactional
    @Query("DELETE FROM ActivityLog a WHERE a.project.id = :projectId")
    int deleteByProjectId(@Param("projectId") Long projectId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ActivityLog a WHERE a.timestamp < :cutoff")
    int deleteOlderThan(@Param("cutoff") LocalDateTime cutoff);
}