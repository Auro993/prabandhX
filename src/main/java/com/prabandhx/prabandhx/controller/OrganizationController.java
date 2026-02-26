package com.prabandhx.prabandhx.controller;

import com.prabandhx.prabandhx.entity.Organization;
import com.prabandhx.prabandhx.service.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    @Autowired
    private OrganizationService organizationService;

    // CREATE
    @PostMapping
    public Organization create(@RequestBody Organization organization) {
        return organizationService.createOrganization(organization);
    }

    // GET ALL
    @GetMapping
    public List<Organization> getAll() {
        return organizationService.getAllOrganizations();
    }

    // GET BY ID  ✅ (THIS WAS MISSING)
    @GetMapping("/{id}")
    public Organization getById(@PathVariable Long id) {
        return organizationService.getOrganizationById(id);
    }

    // UPDATE
    @PutMapping("/{id}")
    public Organization update(@PathVariable Long id,
                               @RequestBody Organization organization) {
        return organizationService.updateOrganization(id, organization);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        organizationService.deleteOrganization(id);
        return "Organization deleted successfully";
    }
}