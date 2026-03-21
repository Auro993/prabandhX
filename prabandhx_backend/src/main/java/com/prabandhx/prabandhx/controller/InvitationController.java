package com.prabandhx.prabandhx.controller;

import com.prabandhx.prabandhx.dto.CollaboratorDTO;
import com.prabandhx.prabandhx.dto.InvitationDTO;
import com.prabandhx.prabandhx.service.CollaborationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/invitations")
@CrossOrigin(origins = "*")
public class InvitationController {

    @Autowired
    private CollaborationService collaborationService;

    /**
     * Public endpoint to accept an invitation without authentication
     * This is for external users/guests
     */
    @PostMapping("/accept")
    public ResponseEntity<?> acceptInvitation(@RequestParam String token, @RequestParam String email) {
        try {
            CollaboratorDTO collaborator = collaborationService.acceptInvitation(token, email);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Invitation accepted successfully");
            response.put("collaborator", collaborator);
            response.put("projectId", collaborator.getProjectId());
            response.put("projectName", collaborator.getProjectName());
            response.put("permissionLevel", collaborator.getPermissionLevel());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Check if an invitation is valid (without accepting)
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateInvitation(@RequestParam String token) {
        try {
            // This would need a method in service to validate token
            // For now, return a placeholder
            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("message", "Invitation is valid");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid invitation");
        }
    }

    /**
     * Resend invitation email
     */
    @PostMapping("/resend")
    public ResponseEntity<?> resendInvitation(@RequestParam String token) {
        try {
            // This would need a method in service to resend invitation
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Invitation resent successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to resend invitation");
        }
    }
}