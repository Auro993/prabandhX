package com.prabandhx.prabandhx.entity;

public enum CollaboratorPermission {
    VIEWER("Can only view"),
    EDITOR("Can edit tasks"),
    UPLOADER("Can upload files"),
    ADMIN("Full control");

    private final String description;

    CollaboratorPermission(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}