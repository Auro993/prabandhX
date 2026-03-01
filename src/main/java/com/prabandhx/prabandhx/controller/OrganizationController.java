package com.prabandhx.prabandhx.controller;

import com.prabandhx.prabandhx.entity.Organization;
import com.prabandhx.prabandhx.service.OrganizationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizations")
@PreAuthorize("hasRole('ADMIN')")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @PostMapping
    public Organization create(@RequestBody Organization organization) {
        return organizationService.createOrganization(organization);
    }

    @GetMapping
    public List<Organization> getAll() {
        return organizationService.getAllOrganizations();
    }

    @GetMapping("/{id}")
    public Organization getById(@PathVariable Long id) {
        return organizationService.getOrganizationById(id);
    }

    @PutMapping("/{id}")
    public Organization update(@PathVariable Long id,
                               @RequestBody Organization organization) {
        return organizationService.updateOrganization(id, organization);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        organizationService.deleteOrganization(id);
        return "Organization deleted successfully";
    }
}