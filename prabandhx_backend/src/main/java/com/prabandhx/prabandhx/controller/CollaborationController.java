package com.prabandhx.prabandhx.controller;

import com.prabandhx.prabandhx.dto.ActivityLogDTO;
import com.prabandhx.prabandhx.dto.CollaboratorDTO;
import com.prabandhx.prabandhx.dto.InvitationDTO;
import com.prabandhx.prabandhx.entity.CollaboratorPermission;
import com.prabandhx.prabandhx.entity.User;
import com.prabandhx.prabandhx.repository.UserRepository;
import com.prabandhx.prabandhx.service.CollaborationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/collaborations")
@CrossOrigin(origins = "*")
public class CollaborationController {

    @Autowired
    private CollaborationService collaborationService;
    
    @Autowired
    private UserRepository userRepository;

    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) auth.getPrincipal()).getUsername();
        }
        throw new RuntimeException("User not authenticated");
    }

    private Long getCurrentUserId() {
        String email = getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return user.getId();
    }

    /**
     * Invite a collaborator to a project
     */
    @PostMapping("/invite")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> inviteCollaborator(@RequestBody InvitationDTO invitation) {
        try {
            String email = getCurrentUserEmail();
            
            // Get the actual user ID from database using email
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
            
            Long userId = user.getId();
            System.out.println("✅ Inviting collaborator - User: " + email + " (ID: " + userId + ")");
            System.out.println("✅ Invitation details - Project ID: " + invitation.getProjectId() + 
                             ", Email: " + invitation.getEmail() + 
                             ", Permission: " + invitation.getPermissionLevel() +
                             ", Expiry: " + invitation.getExpiryDays() + " days");
            
            CollaboratorDTO collaborator = collaborationService.inviteCollaborator(invitation, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Invitation sent successfully");
            response.put("collaborator", collaborator);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Accept an invitation
     */
    @PostMapping("/accept")
    public ResponseEntity<?> acceptInvitation(
            @RequestParam String token,
            @RequestParam String email) {
        try {
            System.out.println("✅ Accepting invitation - Token: " + token + ", Email: " + email);
            
            CollaboratorDTO collaborator = collaborationService.acceptInvitation(token, email);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Invitation accepted successfully");
            response.put("collaborator", collaborator);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get collaborators for a project
     */
    @GetMapping("/project/{projectId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CollaboratorDTO>> getProjectCollaborators(@PathVariable Long projectId) {
        System.out.println("📋 Fetching collaborators for project ID: " + projectId);
        return ResponseEntity.ok(collaborationService.getProjectCollaborators(projectId));
    }

    /**
     * Get projects where current user is a collaborator
     */
    @GetMapping("/my-projects")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CollaboratorDTO>> getMyCollaborations() {
        String email = getCurrentUserEmail();
        System.out.println("📋 Fetching collaborations for user: " + email);
        return ResponseEntity.ok(collaborationService.getUserCollaborations(email));
    }

    /**
     * Update collaborator permissions
     */
    @PutMapping("/{collaboratorId}/permission")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updatePermission(
            @PathVariable Long collaboratorId,
            @RequestParam CollaboratorPermission permission) {
        try {
            Long userId = getCurrentUserId();
            System.out.println("✏️ Updating permission for collaborator ID: " + collaboratorId + 
                             " to: " + permission);
            
            CollaboratorDTO updated = collaborationService.updatePermission(collaboratorId, permission, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Permission updated successfully");
            response.put("collaborator", updated);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Remove a collaborator
     */
    @DeleteMapping("/{collaboratorId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> removeCollaborator(@PathVariable Long collaboratorId) {
        try {
            Long userId = getCurrentUserId();
            System.out.println("🗑️ Removing collaborator ID: " + collaboratorId);
            
            collaborationService.removeCollaborator(collaboratorId, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Collaborator removed successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // ============================================
    // ===== NEW: ADMIN ENDPOINTS =====
    // ============================================

    /**
     * Get all collaborators (Admin only)
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CollaboratorDTO>> getAllCollaborators() {
        System.out.println("👑 Admin fetching all collaborators");
        return ResponseEntity.ok(collaborationService.getAllCollaborators());
    }

    /**
     * Get all collaborators with pagination (Admin only)
     */
    @GetMapping("/admin/all/paginated")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CollaboratorDTO>> getAllCollaboratorsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        System.out.println("👑 Admin fetching collaborators page " + page + " (size: " + size + ")");
        return ResponseEntity.ok(collaborationService.getAllCollaboratorsPaginated(page, size));
    }

    /**
     * Get collaborator statistics (Admin only)
     */
    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getCollaboratorStats() {
        System.out.println("👑 Admin fetching collaborator stats");
        return ResponseEntity.ok(collaborationService.getCollaboratorStats());
    }

    /**
     * Admin delete collaborator (hard delete)
     */
    @DeleteMapping("/admin/{collaboratorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> adminDeleteCollaborator(@PathVariable Long collaboratorId) {
        try {
            System.out.println("👑 Admin deleting collaborator ID: " + collaboratorId);
            collaborationService.adminDeleteCollaborator(collaboratorId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Collaborator deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Admin update collaborator
     */
    @PutMapping("/admin/{collaboratorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> adminUpdateCollaborator(
            @PathVariable Long collaboratorId,
            @RequestParam(required = false) CollaboratorPermission permission,
            @RequestParam(required = false) Boolean isActive) {
        try {
            System.out.println("👑 Admin updating collaborator ID: " + collaboratorId);
            
            CollaboratorDTO updated = collaborationService.adminUpdateCollaborator(collaboratorId, permission, isActive);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Collaborator updated successfully");
            response.put("collaborator", updated);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Search collaborators (Admin only)
     */
    @GetMapping("/admin/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CollaboratorDTO>> searchCollaborators(
            @RequestParam String term) {
        System.out.println("👑 Admin searching collaborators with term: " + term);
        return ResponseEntity.ok(collaborationService.searchCollaborators(term));
    }

    /**
     * Get collaborators by status (Admin only)
     */
    @GetMapping("/admin/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CollaboratorDTO>> getCollaboratorsByStatus(
            @PathVariable String status) {
        System.out.println("👑 Admin fetching collaborators with status: " + status);
        return ResponseEntity.ok(collaborationService.getCollaboratorsByStatus(status));
    }

    /**
     * Get activity logs for a project
     */
    @GetMapping("/project/{projectId}/logs")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ActivityLogDTO>> getProjectLogs(@PathVariable Long projectId) {
        System.out.println("📋 Fetching activity logs for project ID: " + projectId);
        return ResponseEntity.ok(collaborationService.getProjectActivityLogs(projectId));
    }

    /**
     * Get recent activity logs
     */
    @GetMapping("/project/{projectId}/logs/recent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ActivityLogDTO>> getRecentLogs(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "7") int days) {
        System.out.println("📋 Fetching recent activity logs for project ID: " + projectId + 
                         " (last " + days + " days)");
        return ResponseEntity.ok(collaborationService.getRecentActivityLogs(projectId, days));
    }

    /**
     * Check if user has permission
     */
    @GetMapping("/check-permission")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Boolean>> checkPermission(
            @RequestParam Long projectId,
            @RequestParam CollaboratorPermission requiredPermission) {
        String email = getCurrentUserEmail();
        boolean hasPermission = collaborationService.hasPermission(email, projectId, requiredPermission);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("hasPermission", hasPermission);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Clean up expired invitations (admin only)
     */
    @PostMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cleanupExpired() {
        System.out.println("🧹 Cleaning up expired invitations");
        collaborationService.cleanupExpiredInvitations();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Expired invitations cleaned up");
        
        return ResponseEntity.ok(response);
    }
}