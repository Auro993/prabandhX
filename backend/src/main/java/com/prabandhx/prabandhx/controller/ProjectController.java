package com.prabandhx.prabandhx.controller;

import com.prabandhx.prabandhx.dto.ProjectRequestDTO;
import com.prabandhx.prabandhx.dto.ProjectResponseDTO;
import com.prabandhx.prabandhx.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ProjectResponseDTO create(@Valid @RequestBody ProjectRequestDTO dto) {
        return projectService.createProject(dto);
    }

    // ✅ PAGINATION ENABLED
    @GetMapping
    public Page<ProjectResponseDTO> getAll(
            @PageableDefault(size = 5) Pageable pageable) {

        return projectService.getAllProjects(pageable);
    }
}