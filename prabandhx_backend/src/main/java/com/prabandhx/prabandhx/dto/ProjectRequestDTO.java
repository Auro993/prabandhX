package com.prabandhx.prabandhx.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProjectRequestDTO {

    @NotBlank(message = "Project name is required")
    @Size(min = 3, max = 100, message = "Project name must be between 3 and 100 characters")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(min = 5, max = 500, message = "Description must be between 5 and 500 characters")
    private String description;

    // Optional fields for advanced creation
    private Long managerId;
    private String status;

    public ProjectRequestDTO() {}

    public ProjectRequestDTO(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public ProjectRequestDTO(String name, String description, Long managerId, String status) {
        this.name = name;
        this.description = description;
        this.managerId = managerId;
        this.status = status;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ProjectRequestDTO{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", managerId=" + managerId +
                ", status='" + status + '\'' +
                '}';
    }
}