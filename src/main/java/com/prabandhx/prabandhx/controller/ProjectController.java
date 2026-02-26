package com.prabandhx.prabandhx.controller;

import com.prabandhx.prabandhx.entity.Project;
import com.prabandhx.prabandhx.security.JwtUtil;
import com.prabandhx.prabandhx.service.ProjectService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    public Project create(@RequestBody Project project,
                          HttpServletRequest request) {

        String token = request.getHeader("Authorization").substring(7);
        String email = jwtUtil.extractEmail(token);

        return projectService.createProject(project, email);
    }

    @GetMapping
    public List<Project> getAll(HttpServletRequest request) {

        String token = request.getHeader("Authorization").substring(7);
        String email = jwtUtil.extractEmail(token);

        return projectService.getAllProjects(email);
    }
}