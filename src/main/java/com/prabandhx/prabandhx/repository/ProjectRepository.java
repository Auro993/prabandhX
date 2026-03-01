package com.prabandhx.prabandhx.repository;

import com.prabandhx.prabandhx.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    Page<Project> findByOrganizationId(Long organizationId, Pageable pageable);

}