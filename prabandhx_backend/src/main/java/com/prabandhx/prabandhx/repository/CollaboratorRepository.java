package com.prabandhx.prabandhx.repository;

import com.prabandhx.prabandhx.entity.Collaborator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CollaboratorRepository extends JpaRepository<Collaborator, Long> {

    // ===== EXISTING METHODS =====
    
    @Query("SELECT c FROM Collaborator c WHERE c.project.id = :projectId")
    List<Collaborator> findByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT c FROM Collaborator c WHERE c.email = :email")
    List<Collaborator> findByEmail(@Param("email") String email);

    @Query("SELECT c FROM Collaborator c WHERE c.token = :token")
    Optional<Collaborator> findByToken(@Param("token") String token);

    @Query("SELECT c FROM Collaborator c WHERE c.email = :email AND c.isAccepted = true")
    List<Collaborator> findByEmailAndIsAcceptedTrue(@Param("email") String email);

    @Query("SELECT c FROM Collaborator c WHERE c.project.id = :projectId AND c.isActive = true")
    List<Collaborator> findActiveCollaborators(@Param("projectId") Long projectId);

    @Query("SELECT c FROM Collaborator c WHERE c.email = :email AND c.project.id = :projectId")
    Optional<Collaborator> findByEmailAndProjectId(@Param("email") String email, @Param("projectId") Long projectId);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Collaborator c WHERE c.email = :email AND c.project.id = :projectId AND c.isActive = true")
    boolean existsByEmailAndProjectIdAndIsActiveTrue(@Param("email") String email, @Param("projectId") Long projectId);

    @Query("SELECT c FROM Collaborator c WHERE c.expiresAt < :now AND c.isActive = true")
    List<Collaborator> findExpiredInvitations(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(c) FROM Collaborator c WHERE c.project.id = :projectId AND c.isAccepted = true")
    Long countByProjectIdAndIsAcceptedTrue(@Param("projectId") Long projectId);

    // ===== NEW ADMIN METHODS =====

    /**
     * Find all active collaborators (accepted and active)
     */
    @Query("SELECT c FROM Collaborator c WHERE c.isAccepted = true AND c.isActive = true")
    List<Collaborator> findByIsAcceptedTrueAndIsActiveTrue();

    /**
     * Find all pending collaborators (not accepted yet)
     */
    @Query("SELECT c FROM Collaborator c WHERE c.isAccepted = false")
    List<Collaborator> findByIsAcceptedFalse();

    /**
     * Count all active collaborators
     */
    @Query("SELECT COUNT(c) FROM Collaborator c WHERE c.isAccepted = true AND c.isActive = true")
    Long countByIsAcceptedTrueAndIsActiveTrue();

    /**
     * Count all pending collaborators
     */
    @Query("SELECT COUNT(c) FROM Collaborator c WHERE c.isAccepted = false")
    Long countByIsAcceptedFalse();

    /**
     * Search collaborators by email or project name (case insensitive)
     */
    @Query("SELECT c FROM Collaborator c WHERE " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.project.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Collaborator> searchCollaborators(@Param("searchTerm") String searchTerm);

    /**
     * Find collaborators by permission level
     */
    @Query("SELECT c FROM Collaborator c WHERE c.permissionLevel = :permissionLevel")
    List<Collaborator> findByPermissionLevel(@Param("permissionLevel") String permissionLevel);

    /**
     * Count collaborators by project
     */
    @Query("SELECT COUNT(c) FROM Collaborator c WHERE c.project.id = :projectId")
    Long countByProjectId(@Param("projectId") Long projectId);

    /**
     * Find collaborators by inviter
     */
    @Query("SELECT c FROM Collaborator c WHERE c.invitedBy.id = :inviterId")
    List<Collaborator> findByInvitedById(@Param("inviterId") Long inviterId);

    /**
     * Delete expired invitations (batch delete)
     */
    @Query("DELETE FROM Collaborator c WHERE c.expiresAt < :now AND c.isAccepted = false")
    void deleteExpiredInvitations(@Param("now") LocalDateTime now);

    /**
     * Find all collaborators ordered by invited date (descending)
     */
    @Query("SELECT c FROM Collaborator c ORDER BY c.invitedAt DESC")
    List<Collaborator> findAllOrderByInvitedAtDesc();

    /**
     * Find collaborators expiring between dates
     */
    @Query("SELECT c FROM Collaborator c WHERE c.expiresAt BETWEEN :start AND :end")
    List<Collaborator> findCollaboratorsExpiringBetween(
            @Param("start") LocalDateTime start, 
            @Param("end") LocalDateTime end);
}