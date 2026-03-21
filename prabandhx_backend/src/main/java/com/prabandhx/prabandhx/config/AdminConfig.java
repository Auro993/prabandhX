package com.prabandhx.prabandhx.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

@Component
public class AdminConfig {
    
    @Value("${admin.allowed.emails:admin@prabandhx.com}")
    private String allowedEmailsString;
    
    @Value("${admin.allowed.domains:}")
    private String allowedDomainsString;
    
    @Value("${admin.restriction.enabled:true}")
    private boolean restrictionEnabled;
    
    private List<String> allowedEmails = new ArrayList<>();
    private List<String> allowedDomains = new ArrayList<>();
    
    @PostConstruct
    public void init() {
        // Parse comma-separated emails
        if (allowedEmailsString != null && !allowedEmailsString.isEmpty()) {
            allowedEmails = Arrays.asList(allowedEmailsString.split(","));
        }
        
        // Parse comma-separated domains
        if (allowedDomainsString != null && !allowedDomainsString.isEmpty()) {
            allowedDomains = Arrays.asList(allowedDomainsString.split(","));
        }
        
        System.out.println("✅ Admin restriction enabled: " + restrictionEnabled);
        System.out.println("✅ Allowed admin emails: " + allowedEmails);
        System.out.println("✅ Allowed admin domains: " + allowedDomains);
    }
    
    public boolean isAllowedAdmin(String email) {
        if (!restrictionEnabled) {
            return true; // Restriction disabled, anyone can be admin
        }
        
        if (email == null || email.isEmpty()) {
            return false;
        }
        
        // Check exact email match
        if (allowedEmails.contains(email)) {
            return true;
        }
        
        // Check domain match
        for (String domain : allowedDomains) {
            if (email.endsWith(domain.trim())) {
                return true;
            }
        }
        
        return false;
    }
    
    public List<String> getAllowedEmails() {
        return allowedEmails;
    }
    
    public List<String> getAllowedDomains() {
        return allowedDomains;
    }
    
    public boolean isRestrictionEnabled() {
        return restrictionEnabled;
    }
}