package com.prabandhx.prabandhx.service;

import com.prabandhx.prabandhx.dto.ActivityLogDTO;
import com.prabandhx.prabandhx.dto.CollaboratorDTO;
import com.prabandhx.prabandhx.dto.InvitationDTO;
import com.prabandhx.prabandhx.entity.*;
import com.prabandhx.prabandhx.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CollaborationService {

    @Autowired
    private CollaboratorRepository collaboratorRepository;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private ActivityLogService activityLogService;

    @Value("${app.base-url:http://localhost:5173}")
    private String baseUrl;

    // ===============================
    // CONVERTER METHODS
    // ===============================

    private CollaboratorDTO convertToDTO(Collaborator collaborator) {
        CollaboratorDTO dto = new CollaboratorDTO();
        dto.setId(collaborator.getId());
        dto.setProjectId(collaborator.getProjectId());
        dto.setProjectName(collaborator.getProjectName());
        dto.setEmail(collaborator.getEmail());
        dto.setPermissionLevel(collaborator.getPermissionLevel());
        dto.setInvitedById(collaborator.getInvitedById());
        dto.setInvitedByName(collaborator.getInvitedByName());
        dto.setInvitedAt(collaborator.getInvitedAt());
        dto.setExpiresAt(collaborator.getExpiresAt());
        dto.setToken(collaborator.getToken());
        dto.setIsAccepted(collaborator.getIsAccepted());
        dto.setAcceptedAt(collaborator.getAcceptedAt());
        dto.setIsActive(collaborator.getIsActive());
        dto.setIsExpired(collaborator.isExpired());
        
        if (collaborator.getToken() != null) {
            dto.setInviteUrl(baseUrl + "/accept-invite?token=" + collaborator.getToken());
        }
        
        return dto;
    }

    private ActivityLogDTO convertToDTO(ActivityLog log) {
        ActivityLogDTO dto = new ActivityLogDTO();
        dto.setId(log.getId());
        dto.setProjectId(log.getProjectId());
        dto.setProjectName(log.getProjectName());
        dto.setUserEmail(log.getUserEmail());
        dto.setUserName(log.getUserName());
        dto.setAction(log.getAction());
        dto.setActionType(log.getActionType());
        dto.setDetails(log.getDetails());
        dto.setTimestamp(log.getTimestamp());
        
        // Format timestamp for display
        if (log.getTimestamp() != null) {
            dto.setFormattedTimestamp(log.getTimestamp().format(
                java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
            ));
        }
        
        return dto;
    }

    // ===============================
    // INVITE COLLABORATOR
    // ===============================

    @Transactional
    public CollaboratorDTO inviteCollaborator(InvitationDTO invitation, Long inviterId) {
        // Get project
        Project project = projectRepository.findById(invitation.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // Get inviter
        User inviter = userRepository.findById(inviterId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if already invited
        if (collaboratorRepository.existsByEmailAndProjectIdAndIsActiveTrue(
                invitation.getEmail(), invitation.getProjectId())) {
            throw new RuntimeException("User already invited to this project");
        }

        // Generate unique token
        String token = UUID.randomUUID().toString();

        // Create collaborator
        Collaborator collaborator = new Collaborator();
        collaborator.setProject(project);
        collaborator.setEmail(invitation.getEmail());
        collaborator.setPermissionLevel(invitation.getPermissionLevel());
        collaborator.setInvitedBy(inviter);
        collaborator.setInvitedAt(LocalDateTime.now());
        
        if (invitation.getExpiryDays() != null && invitation.getExpiryDays() > 0) {
            collaborator.setExpiresAt(LocalDateTime.now().plusDays(invitation.getExpiryDays()));
        }
        
        collaborator.setToken(token);
        collaborator.setIsAccepted(false);
        collaborator.setIsActive(true);

        Collaborator savedCollaborator = collaboratorRepository.save(collaborator);

        // Send invitation email
        String inviteUrl = baseUrl + "/accept-invite?token=" + token;
        emailService.sendInvitationEmail(
            invitation.getEmail(),
            project.getName(),
            inviteUrl,
            invitation.getPermissionLevel().toString(),
            invitation.getExpiryDays() != null ? invitation.getExpiryDays() : 7
        );

        // Log activity using ActivityLogService
        activityLogService.logActivity(
            inviter.getId(),
            "INVITE_COLLABORATOR",
            "Invited collaborator",
            "COLLABORATOR",
            savedCollaborator.getId(),
            invitation.getEmail(),
            project.getId(),
            "Invited " + invitation.getEmail() + " to project '" + project.getName() + 
                "' as " + invitation.getPermissionLevel() + 
                (invitation.getExpiryDays() != null ? " (expires in " + invitation.getExpiryDays() + " days)" : ""),
            null
        );

        return convertToDTO(savedCollaborator);
    }

    // ===============================
    // ACCEPT INVITATION
    // ===============================

    @Transactional
    public CollaboratorDTO acceptInvitation(String token, String acceptedEmail) {
        Collaborator collaborator = collaboratorRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid invitation token"));

        // Check if expired
        if (collaborator.isExpired()) {
            collaborator.setIsActive(false);
            collaboratorRepository.save(collaborator);
            throw new RuntimeException("Invitation has expired");
        }

        // Check if already accepted
        if (collaborator.getIsAccepted()) {
            throw new RuntimeException("Invitation already accepted");
        }

        // Verify email matches
        if (!collaborator.getEmail().equalsIgnoreCase(acceptedEmail)) {
            throw new RuntimeException("Email does not match invitation");
        }

        // Accept invitation
        collaborator.setIsAccepted(true);
        collaborator.setAcceptedAt(LocalDateTime.now());

        Collaborator updatedCollaborator = collaboratorRepository.save(collaborator);

        // Log activity
        activityLogService.logActivity(
            null, // User might not have an account yet
            "ACCEPT_INVITE",
            "Accepted invitation",
            "COLLABORATOR",
            updatedCollaborator.getId(),
            acceptedEmail,
            collaborator.getProjectId(),
            "Accepted invitation to project '" + collaborator.getProjectName() + "' as " + 
                collaborator.getPermissionLevel(),
            null
        );

        return convertToDTO(updatedCollaborator);
    }

    // ===============================
    // GET PROJECT COLLABORATORS
    // ===============================

    public List<CollaboratorDTO> getProjectCollaborators(Long projectId) {
        return collaboratorRepository.findByProjectId(projectId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ===============================
    // GET USER'S COLLABORATIONS
    // ===============================

    public List<CollaboratorDTO> getUserCollaborations(String email) {
        return collaboratorRepository.findByEmailAndIsAcceptedTrue(email)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ===============================
    // ===== ADMIN METHODS =====
    // ===============================

    /**
     * Get all collaborators (Admin only)
     */
    public List<CollaboratorDTO> getAllCollaborators() {
        return collaboratorRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all collaborators with pagination (Admin only)
     */
    public Page<CollaboratorDTO> getAllCollaboratorsPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return collaboratorRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get collaborator statistics (Admin only)
     */
    public Map<String, Object> getCollaboratorStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long total = collaboratorRepository.count();
        long active = collaboratorRepository.countByIsAcceptedTrueAndIsActiveTrue();
        long pending = collaboratorRepository.countByIsAcceptedFalse();
        
        // Count expired invitations
        List<Collaborator> expired = collaboratorRepository.findExpiredInvitations(LocalDateTime.now());
        long expiredCount = expired.size();
        
        stats.put("total", total);
        stats.put("active", active);
        stats.put("pending", pending);
        stats.put("expired", expiredCount);
        
        return stats;
    }

    /**
     * Admin delete collaborator (hard delete)
     */
    @Transactional
    public void adminDeleteCollaborator(Long collaboratorId) {
        Collaborator collaborator = collaboratorRepository.findById(collaboratorId)
                .orElseThrow(() -> new RuntimeException("Collaborator not found"));
        
        // Log before deleting
        activityLogService.logActivity(
            null,
            "ADMIN_DELETE_COLLABORATOR",
            "Admin deleted collaborator",
            "COLLABORATOR",
            collaboratorId,
            collaborator.getEmail(),
            collaborator.getProjectId(),
            "Admin deleted collaborator: " + collaborator.getEmail(),
            null
        );
        
        // Hard delete from database
        collaboratorRepository.delete(collaborator);
    }

    /**
     * Admin update any collaborator
     */
    @Transactional
    public CollaboratorDTO adminUpdateCollaborator(Long collaboratorId, CollaboratorPermission newPermission, Boolean isActive) {
        Collaborator collaborator = collaboratorRepository.findById(collaboratorId)
                .orElseThrow(() -> new RuntimeException("Collaborator not found"));
        
        CollaboratorPermission oldPermission = collaborator.getPermissionLevel();
        Boolean oldActive = collaborator.getIsActive();
        
        if (newPermission != null) {
            collaborator.setPermissionLevel(newPermission);
        }
        
        if (isActive != null) {
            collaborator.setIsActive(isActive);
        }
        
        Collaborator updatedCollaborator = collaboratorRepository.save(collaborator);
        
        // Log activity
        String changes = "";
        if (newPermission != null && !newPermission.equals(oldPermission)) {
            changes += "permission from " + oldPermission + " to " + newPermission;
        }
        if (isActive != null && !isActive.equals(oldActive)) {
            if (!changes.isEmpty()) changes += " and ";
            changes += "status from " + (oldActive ? "active" : "inactive") + " to " + 
                      (isActive ? "active" : "inactive");
        }
        
        activityLogService.logActivity(
            null,
            "ADMIN_UPDATE_COLLABORATOR",
            "Admin updated collaborator",
            "COLLABORATOR",
            collaboratorId,
            collaborator.getEmail(),
            collaborator.getProjectId(),
            "Admin updated collaborator " + collaborator.getEmail() + ": " + changes,
            null
        );
        
        return convertToDTO(updatedCollaborator);
    }

    /**
     * Search collaborators by email or project (Admin only)
     */
    public List<CollaboratorDTO> searchCollaborators(String searchTerm) {
        return collaboratorRepository.searchCollaborators(searchTerm)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get collaborators by status (Admin only)
     */
    public List<CollaboratorDTO> getCollaboratorsByStatus(String status) {
        List<Collaborator> collaborators;
        
        switch (status.toLowerCase()) {
            case "active":
                collaborators = collaboratorRepository.findByIsAcceptedTrueAndIsActiveTrue();
                break;
            case "pending":
                collaborators = collaboratorRepository.findByIsAcceptedFalse();
                break;
            case "expired":
                collaborators = collaboratorRepository.findExpiredInvitations(LocalDateTime.now());
                break;
            default:
                collaborators = collaboratorRepository.findAll();
        }
        
        return collaborators.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ===============================
    // UPDATE COLLABORATOR PERMISSIONS
    // ===============================

    @Transactional
    public CollaboratorDTO updatePermission(Long collaboratorId, CollaboratorPermission newPermission, Long updaterId) {
        Collaborator collaborator = collaboratorRepository.findById(collaboratorId)
                .orElseThrow(() -> new RuntimeException("Collaborator not found"));

        CollaboratorPermission oldPermission = collaborator.getPermissionLevel();
        collaborator.setPermissionLevel(newPermission);

        Collaborator updatedCollaborator = collaboratorRepository.save(collaborator);

        // Log activity
        User updater = userRepository.findById(updaterId).orElse(null);
        activityLogService.logActivity(
            updaterId,
            "UPDATE_COLLABORATOR_PERMISSION",
            "Updated collaborator permission",
            "COLLABORATOR",
            collaboratorId,
            collaborator.getEmail(),
            collaborator.getProjectId(),
            "Changed permission for " + collaborator.getEmail() + 
            " from " + oldPermission + " to " + newPermission,
            null
        );

        return convertToDTO(updatedCollaborator);
    }

    // ===============================
    // REMOVE COLLABORATOR
    // ===============================

    @Transactional
    public void removeCollaborator(Long collaboratorId, Long removerId) {
        Collaborator collaborator = collaboratorRepository.findById(collaboratorId)
                .orElseThrow(() -> new RuntimeException("Collaborator not found"));

        collaborator.setIsActive(false);
        collaboratorRepository.save(collaborator);

        // Log activity
        User remover = userRepository.findById(removerId).orElse(null);
        activityLogService.logActivity(
            removerId,
            "REMOVE_COLLABORATOR",
            "Removed collaborator",
            "COLLABORATOR",
            collaboratorId,
            collaborator.getEmail(),
            collaborator.getProjectId(),
            "Removed collaborator: " + collaborator.getEmail(),
            null
        );
    }

    // ===============================
    // GET ACTIVITY LOGS
    // ===============================

    public List<ActivityLogDTO> getProjectActivityLogs(Long projectId) {
        return activityLogRepository.findByProjectIdOrderByTimestampDesc(projectId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ActivityLogDTO> getRecentActivityLogs(Long projectId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        
        // Using the correct method that exists in repository
        return activityLogRepository.findRecentActivityByProject(projectId, since)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ===============================
    // CHECK PERMISSION
    // ===============================

    public boolean hasPermission(String email, Long projectId, CollaboratorPermission requiredPermission) {
        // Check if user is project manager
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project != null && project.getManager() != null && 
            project.getManager().getEmail().equalsIgnoreCase(email)) {
            return true; // Manager has full access
        }

        // Check collaborator permissions
        Collaborator collaborator = collaboratorRepository
                .findByEmailAndProjectId(email, projectId).orElse(null);

        if (collaborator == null || !collaborator.getIsAccepted() || !collaborator.getIsActive()) {
            return false;
        }

        // Permission hierarchy
        switch (requiredPermission) {
            case VIEWER:
                return true; // All accepted collaborators can view
            case UPLOADER:
                return collaborator.getPermissionLevel() == CollaboratorPermission.UPLOADER ||
                       collaborator.getPermissionLevel() == CollaboratorPermission.EDITOR ||
                       collaborator.getPermissionLevel() == CollaboratorPermission.ADMIN;
            case EDITOR:
                return collaborator.getPermissionLevel() == CollaboratorPermission.EDITOR ||
                       collaborator.getPermissionLevel() == CollaboratorPermission.ADMIN;
            case ADMIN:
                return collaborator.getPermissionLevel() == CollaboratorPermission.ADMIN;
            default:
                return false;
        }
    }

    // ===============================
    // CLEANUP EXPIRED INVITATIONS
    // ===============================

    @Transactional
    public void cleanupExpiredInvitations() {
        List<Collaborator> expired = collaboratorRepository.findExpiredInvitations(LocalDateTime.now());
        for (Collaborator collab : expired) {
            collab.setIsActive(false);
            collaboratorRepository.save(collab);
            
            activityLogService.logActivity(
                null,
                "EXPIRE_INVITATION",
                "Invitation expired",
                "COLLABORATOR",
                collab.getId(),
                collab.getEmail(),
                collab.getProjectId(),
                "Invitation for " + collab.getEmail() + " expired",
                null
            );
        }
    }

    // ===============================
    // PRIVATE HELPER METHODS
    // ===============================

    private void logActivity(Project project, String userEmail, String actionType, String details) {
        // This method is kept for backward compatibility
        // but internally uses ActivityLogService
        userRepository.findByEmail(userEmail).ifPresent(user -> {
            activityLogService.logActivity(
                user.getId(),
                actionType,
                getActionDisplayName(actionType),
                "COLLABORATOR",
                null,
                null,
                project != null ? project.getId() : null,
                details,
                null
            );
        });
    }
    
    private String getActionDisplayName(String actionType) {
        switch (actionType) {
            case "INVITE": return "Invitation Sent";
            case "ACCEPT": return "Invitation Accepted";
            case "UPDATE": return "Permissions Updated";
            case "REMOVE": return "Collaborator Removed";
            case "EXPIRE": return "Invitation Expired";
            case "DELETE": return "Collaborator Deleted";
            case "ADMIN_UPDATE": return "Admin Updated";
            default: return actionType;
        }
    }
}