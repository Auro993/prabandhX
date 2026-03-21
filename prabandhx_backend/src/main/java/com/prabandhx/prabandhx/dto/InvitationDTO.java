package com.prabandhx.prabandhx.dto;

import com.prabandhx.prabandhx.entity.CollaboratorPermission;

public class InvitationDTO {
    private String email;
    private Long projectId;
    private CollaboratorPermission permissionLevel;
    private Integer expiryDays;

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public CollaboratorPermission getPermissionLevel() {
        return permissionLevel;
    }

    public void setPermissionLevel(CollaboratorPermission permissionLevel) {
        this.permissionLevel = permissionLevel;
    }

    public Integer getExpiryDays() {
        return expiryDays;
    }

    public void setExpiryDays(Integer expiryDays) {
        this.expiryDays = expiryDays;
    }
}