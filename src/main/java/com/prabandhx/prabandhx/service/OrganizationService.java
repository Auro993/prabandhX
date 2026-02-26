package com.prabandhx.prabandhx.service;

import com.prabandhx.prabandhx.entity.Organization;
import com.prabandhx.prabandhx.repository.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganizationService {

    @Autowired
    private OrganizationRepository organizationRepository;

    // CREATE
    public Organization createOrganization(Organization organization) {
        return organizationRepository.save(organization);
    }

    // GET ALL
    public List<Organization> getAllOrganizations() {
        return organizationRepository.findAll();
    }

    // GET BY ID
    public Organization getOrganizationById(Long id) {
        return organizationRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Organization not found with id: " + id));
    }

    // UPDATE
    public Organization updateOrganization(Long id, Organization updatedOrg) {
        Organization existingOrg = getOrganizationById(id);

        existingOrg.setName(updatedOrg.getName());
        // add other fields here if you have

        return organizationRepository.save(existingOrg);
    }

    // DELETE
    public void deleteOrganization(Long id) {
        Organization existingOrg = getOrganizationById(id);
        organizationRepository.delete(existingOrg);
    }
}