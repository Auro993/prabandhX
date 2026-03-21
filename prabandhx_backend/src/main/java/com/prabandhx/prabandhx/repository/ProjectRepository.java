package com.prabandhx.prabandhx.repository;

import com.prabandhx.prabandhx.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // Find by organization ID (paginated)
    @Query("SELECT p FROM Project p WHERE p.organization.id = :orgId")
    Page<Project> findByOrganizationId(@Param("orgId") Long organizationId, Pageable pageable);
    
    // Find by organization ID (all)
    @Query("SELECT p FROM Project p WHERE p.organization.id = :orgId")
    List<Project> findAllByOrganizationId(@Param("orgId") Long organizationId);
    
    // Find by manager ID
    @Query("SELECT p FROM Project p WHERE p.manager.id = :managerId")
    List<Project> findByManagerId(@Param("managerId") Long managerId);
    
    // Find by status
    List<Project> findByStatus(String status);
    
    // Find by organization ID and status
    @Query("SELECT p FROM Project p WHERE p.organization.id = :orgId AND p.status = :status")
    List<Project> findByOrganizationIdAndStatus(@Param("orgId") Long orgId, @Param("status") String status);
    
    // Count by organization ID
    @Query("SELECT COUNT(p) FROM Project p WHERE p.organization.id = :orgId")
    Long countByOrganizationId(@Param("orgId") Long orgId);
    
    // Count by organization ID and status
    @Query("SELECT COUNT(p) FROM Project p WHERE p.organization.id = :orgId AND p.status = :status")
    Long countByOrganizationIdAndStatus(@Param("orgId") Long orgId, @Param("status") String status);
}