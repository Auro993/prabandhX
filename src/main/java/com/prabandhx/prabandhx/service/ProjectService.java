package com.prabandhx.prabandhx.service;

import com.prabandhx.prabandhx.entity.*;
import com.prabandhx.prabandhx.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    public Project createProject(Project project, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow();

        project.setOrganization(user.getOrganization());

        return projectRepository.save(project);
    }

    public List<Project> getAllProjects(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow();

        return projectRepository
                .findByOrganizationId(user.getOrganization().getId());
    }
} 