package com.prabandhx.prabandhx.service;

import com.prabandhx.prabandhx.dto.ProjectRequestDTO;
import com.prabandhx.prabandhx.dto.ProjectResponseDTO;
import com.prabandhx.prabandhx.entity.Project;
import com.prabandhx.prabandhx.entity.User;
import com.prabandhx.prabandhx.repository.ProjectRepository;
import com.prabandhx.prabandhx.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository,
                          UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    private User getLoggedInUser() {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public ProjectResponseDTO createProject(ProjectRequestDTO dto) {

        User user = getLoggedInUser();

        Project project = new Project();
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setOrganization(user.getOrganization());

        Project savedProject = projectRepository.save(project);

        return mapToDTO(savedProject);
    }

    // ✅ PAGINATED GET
    public Page<ProjectResponseDTO> getAllProjects(Pageable pageable) {

        User user = getLoggedInUser();

        return projectRepository
                .findByOrganizationId(
                        user.getOrganization().getId(),
                        pageable
                )
                .map(this::mapToDTO);
    }

    private ProjectResponseDTO mapToDTO(Project project) {
        return new ProjectResponseDTO(
                project.getId(),
                project.getName(),
                project.getDescription()
        );
    }
}