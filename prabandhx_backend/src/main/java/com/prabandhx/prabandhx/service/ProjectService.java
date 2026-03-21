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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository,
                          UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    // ===============================
    // HELPER METHODS
    // ===============================

    private User getLoggedInUser() {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    private void validateUserOrganization(User user) {
        if (user.getOrganization() == null) {
            throw new RuntimeException("User does not belong to any organization");
        }
    }

    private ProjectResponseDTO mapToDTO(Project project) {
        return new ProjectResponseDTO(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getCreatedAt(),
                project.getUpdatedAt(),
                project.getOrganizationId(),
                project.getOrganizationName(),
                project.getManagerId(),
                project.getManagerName(),
                project.getTaskCount()
        );
    }

    // ===============================
    // CREATE OPERATIONS
    // ===============================

    @Transactional
    public ProjectResponseDTO createProject(ProjectRequestDTO dto) {
        try {
            User user = getLoggedInUser();
            validateUserOrganization(user);

            Project project = new Project();
            project.setName(dto.getName());
            project.setDescription(dto.getDescription());
            project.setOrganization(user.getOrganization());
            project.setManager(user);
            project.setCreatedAt(LocalDateTime.now());
            project.setUpdatedAt(LocalDateTime.now());
            
            if (dto.getStatus() != null && !dto.getStatus().isEmpty()) {
                project.setStatus(dto.getStatus());
            } else {
                project.setStatus("ACTIVE");
            }

            if (dto.getManagerId() != null) {
                User manager = userRepository.findById(dto.getManagerId())
                        .orElseThrow(() -> new RuntimeException("Manager not found with id: " + dto.getManagerId()));
                
                if (!manager.getOrganization().getId().equals(user.getOrganization().getId())) {
                    throw new RuntimeException("Manager must belong to the same organization");
                }
                project.setManager(manager);
            }

            Project savedProject = projectRepository.save(project);
            System.out.println("✅ Project created: " + savedProject.getName());

            return mapToDTO(savedProject);
            
        } catch (Exception e) {
            throw new RuntimeException("Error creating project: " + e.getMessage());
        }
    }

    // ===============================
    // READ OPERATIONS
    // ===============================

    public List<ProjectResponseDTO> getAllProjectsList() {
        try {
            User user = getLoggedInUser();
            validateUserOrganization(user);

            Long orgId = user.getOrganization().getId();
            List<Project> projects = projectRepository.findAllByOrganizationId(orgId);
            
            return projects.stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            throw new RuntimeException("Error fetching projects: " + e.getMessage());
        }
    }

    public Page<ProjectResponseDTO> getAllProjects(Pageable pageable) {
        try {
            User user = getLoggedInUser();
            validateUserOrganization(user);

            Long orgId = user.getOrganization().getId();
            return projectRepository.findByOrganizationId(orgId, pageable)
                    .map(this::mapToDTO);
                    
        } catch (Exception e) {
            throw new RuntimeException("Error fetching projects: " + e.getMessage());
        }
    }

    public ProjectResponseDTO getProjectById(Long id) {
        try {
            User user = getLoggedInUser();
            
            Project project = projectRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));

            if (!project.getOrganization().getId().equals(user.getOrganization().getId())) {
                throw new RuntimeException("You don't have permission to view this project");
            }

            return mapToDTO(project);
            
        } catch (Exception e) {
            throw new RuntimeException("Error fetching project: " + e.getMessage());
        }
    }

    public List<ProjectResponseDTO> getProjectsByStatus(String status) {
        try {
            User user = getLoggedInUser();
            validateUserOrganization(user);

            Long orgId = user.getOrganization().getId();
            List<Project> projects = projectRepository.findByOrganizationIdAndStatus(orgId, status);
            
            return projects.stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            throw new RuntimeException("Error fetching projects by status: " + e.getMessage());
        }
    }

    // ===============================
    // UPDATE OPERATIONS
    // ===============================

    @Transactional
    public ProjectResponseDTO updateProject(Long id, ProjectRequestDTO dto) {
        try {
            User user = getLoggedInUser();
            
            Project project = projectRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));

            if (!project.getOrganization().getId().equals(user.getOrganization().getId())) {
                throw new RuntimeException("You don't have permission to update this project");
            }

            if (dto.getName() != null && !dto.getName().isEmpty()) {
                project.setName(dto.getName());
            }
            if (dto.getDescription() != null && !dto.getDescription().isEmpty()) {
                project.setDescription(dto.getDescription());
            }
            if (dto.getStatus() != null && !dto.getStatus().isEmpty()) {
                project.setStatus(dto.getStatus());
            }
            
            if (dto.getManagerId() != null) {
                User manager = userRepository.findById(dto.getManagerId())
                        .orElseThrow(() -> new RuntimeException("Manager not found with id: " + dto.getManagerId()));
                
                if (!manager.getOrganization().getId().equals(user.getOrganization().getId())) {
                    throw new RuntimeException("Manager must belong to the same organization");
                }
                project.setManager(manager);
            }

            project.setUpdatedAt(LocalDateTime.now());

            Project updatedProject = projectRepository.save(project);
            return mapToDTO(updatedProject);
            
        } catch (Exception e) {
            throw new RuntimeException("Error updating project: " + e.getMessage());
        }
    }

    @Transactional
    public ProjectResponseDTO updateProjectStatus(Long id, String status) {
        try {
            User user = getLoggedInUser();
            
            Project project = projectRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));

            if (!project.getOrganization().getId().equals(user.getOrganization().getId())) {
                throw new RuntimeException("You don't have permission to update this project");
            }

            project.setStatus(status);
            project.setUpdatedAt(LocalDateTime.now());

            Project updatedProject = projectRepository.save(project);
            return mapToDTO(updatedProject);
            
        } catch (Exception e) {
            throw new RuntimeException("Error updating project status: " + e.getMessage());
        }
    }

    // ===============================
    // DELETE OPERATIONS
    // ===============================

    @Transactional
    public void deleteProject(Long id) {
        try {
            User user = getLoggedInUser();
            
            Project project = projectRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));

            if (!project.getOrganization().getId().equals(user.getOrganization().getId())) {
                throw new RuntimeException("You don't have permission to delete this project");
            }

            projectRepository.delete(project);
            System.out.println("✅ Project deleted: " + project.getName());
            
        } catch (Exception e) {
            throw new RuntimeException("Error deleting project: " + e.getMessage());
        }
    }

    @Transactional
    public void deleteProjectsByOrganization(Long organizationId) {
        try {
            User user = getLoggedInUser();
            
            // FIXED: Only allow if user belongs to this organization
            if (!user.getOrganization().getId().equals(organizationId)) {
                throw new RuntimeException("You don't have permission to delete projects from this organization");
            }

            List<Project> projects = projectRepository.findAllByOrganizationId(organizationId);
            projectRepository.deleteAll(projects);
            System.out.println("✅ Deleted " + projects.size() + " projects from organization ID: " + organizationId);
            
        } catch (Exception e) {
            throw new RuntimeException("Error deleting projects: " + e.getMessage());
        }
    }

    // ===============================
    // COUNT OPERATIONS
    // ===============================

    public Long countProjectsByOrganization() {
        try {
            User user = getLoggedInUser();
            validateUserOrganization(user);
            return projectRepository.countByOrganizationId(user.getOrganization().getId());
            
        } catch (Exception e) {
            throw new RuntimeException("Error counting projects: " + e.getMessage());
        }
    }

    public Long countProjectsByStatus(String status) {
        try {
            User user = getLoggedInUser();
            validateUserOrganization(user);
            return projectRepository.countByOrganizationIdAndStatus(user.getOrganization().getId(), status);
            
        } catch (Exception e) {
            throw new RuntimeException("Error counting projects by status: " + e.getMessage());
        }
    }

    // ===============================
    // VALIDATION
    // ===============================

    public boolean existsById(Long id) {
        return projectRepository.existsById(id);
    }

    public void validateProjectOwnership(Long projectId) {
        User user = getLoggedInUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getOrganization().getId().equals(user.getOrganization().getId())) {
            throw new RuntimeException("You don't have permission to access this project");
        }
    }
}